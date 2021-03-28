#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "../include/ngx_config.h"
#include "../include/ngx_log.h"
#include "../include/ngx_global.h"
#include "../include/ngx_proc.h"


char **arg = NULL;
char *env = NULL;
int envsize = 0;

pid_t ngx_pid;
pid_t ngx_parent;

int ngx_process;

sig_atomic_t ngx_reap;

const char *ngx_conf_path = "/data/data/com.termux/files/home/proj/cppcode/ngx-server-framework/nginx.conf";

const char *ngx_log_path = "/data/data/com.termux/files/home/proj/cppcode/ngx-server-framework/build/nginx.log";


int main(int argc, char *argv[])
{
    ngx_log_init(ngx_log_path);

    ngx_log_info("nginx server starting...\n");

    arg = (char**)argv;

    ngx_pid = getpid();
    ngx_parent = getppid();

    ngx_process = NGX_MASTER_PROCESS;
    
    // init signal
    ngx_signal_init();
    
    // create daemon process
    if(ngx_daemon() != 0) exit(EXIT_FAILURE);

    // init settings process title
    ngx_init_setproctitle();
    
    NgxConfig *config = NgxConfig::getInstance();
    if(!config->load(ngx_conf_path)) {
        exit(EXIT_FAILURE);
    }

    // create worker process
    ngx_master_proc_cycle();
    
    if(env != NULL){
        delete []env;
        env = NULL;
    }

    printf("goodbye!\n");
    return 0;

}
