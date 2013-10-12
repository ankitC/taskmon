LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := reservationFramework
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := set_reserve.c
LOCAL_LDLIBS    := -llog 
LOCAL_C_INCLUDES 	:= "/home/akshita/648/kernel/usr/include"

include $(BUILD_SHARED_LIBRARY)