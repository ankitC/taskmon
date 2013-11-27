#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <asm/unistd.h>
#include <linux/types.h>
#include <linux/time.h>
#include <sys/syscall.h>

JNIEXPORT int JNICALL Java_com_example_taskmonv4_MainActivity_setReserve(
                JNIEnv *env, jobject obj, jint epid, jint cSec, long long cNSec,
                jint tSec, long long tNSec,jint eprio)
{

	pid_t pid = (pid_t)epid;
	struct timespec ctime;
	struct timespec ttime;

	ctime.tv_sec = cSec;
	ctime.tv_nsec = cNSec;

	ttime.tv_sec = tSec;
	ttime.tv_nsec = tNSec;
	unsigned int prio = (unsigned int)eprio;

	return  syscall(__NR_set_reserve, pid, ctime, ttime, prio);
}

JNIEXPORT int JNICALL Java_com_example_taskmonv4_MainActivity_cancelReserve(
                JNIEnv *env, jobject obj, jint epid)
{
	pid_t pid = (pid_t)epid;
	return  syscall(__NR_cancel_reserve, pid);
}
