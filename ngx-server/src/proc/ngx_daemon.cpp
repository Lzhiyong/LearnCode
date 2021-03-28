#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>

#include "../include/ngx_log.h"
#include "../include/ngx_proc.h"
#include "../include/ngx_global.h"

int ngx_daemon()
{
    // (1)创建守护进程的第一步，fork()一个子进程出来
    // fork()出来这个子进程才会成为咱们这里讲解的master进程
    switch (fork()) {  
    case -1:
        //创建子进程失败
        ngx_log_error("%s\n", strerror(errno));
        return -1;
    case 0:
        //子进程，走到这里直接break;
        break;
    default:
        //父进程以往 直接退出exit(0) 现在希望回到主流程去释放一些资源
        return 1;  //父进程直接返回1；
    } 

    // 只有fork()出来的子进程才能走到这个流程
    ngx_parent = ngx_pid;     //ngx_pid是原来父进程的id，因为这里是子进程，所以子进程的ngx_parent设置为原来父进程的pid
    ngx_pid = getpid();       //当前子进程的id要重新取得
    
    // (2)脱离终端，终端关闭，将跟此子进程无关
    if (setsid() == -1) {
        ngx_log_error("%s\n", strerror(errno));
        return -1;
    }

    // (3)设置为0，不要让它来限制文件权限，以免引起混乱
    umask(0); 

    // (4)打开空设备，以读写方式打开
    int fd = open("/dev/null", O_RDWR);
    if (fd == -1) {
        goto error;
    }
    
    // 先关闭STDIN_FILENO[这是规矩，已经打开的描述符，动他之前，先close]，类似于指针指向null，让/dev/null成为标准输入
    if (dup2(fd, STDIN_FILENO) == -1) {
        goto error;
    }
    
    // 再关闭STDIN_FILENO，类似于指针指向null，让/dev/null成为标准输出
    if (dup2(fd, STDOUT_FILENO) == -1) {
        goto error;
    }
    
    // fd应该是3，这个应该成立
    if (fd > STDERR_FILENO) {
        //释放资源这样这个文件描述符就可以被复用；不然这个数字【文件描述符】会被一直占着
        if (close(fd) == -1) {
            goto error;
        }
    }
    return 0; //子进程返回0
error:
    ngx_log_error("%s\n", strerror(errno));
    return -1;
}

