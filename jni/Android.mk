LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include c:\Programs\android\opencv\sdk\native\jni\OpenCV.mk

include $(BUILD_SHARED_LIBRARY)