/*
 * Copyright (C) 2009-2010 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>
#include <ctype.h>
#include <fcntl.h>
#include <errno.h>
#include <utime.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/un.h>

#include <cutils/sockets.h>
#include <cutils/log.h>
#include <cutils/properties.h>

#include <private/android_filesystem_config.h>

#if INCLUDE_SYS_MOUNT_FOR_STATFS
#include <sys/mount.h>
#else
#include <sys/statfs.h>
#endif

#define LOG_TAG       "scand"
#define BUFFER_MAX    1024  /* input buffer for commands */
#define TOKEN_MAX     8     /* max number of arguments in buffer */
#define SOCKET_PATH   "/dev/socket/scand"

static int readx(int s, void *_buf, int count);
static int writex(int s, const void *_buf, int count);
static int execute(int s, char cmd[BUFFER_MAX]);
static void daemonize(void);
int main(const int argc, const char *argv[]);

struct cmdinfo {
    const char *name;
    unsigned numargs;
    int (*func)(char **arg, char reply[REPLY_MAX]);
};
