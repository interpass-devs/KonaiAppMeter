/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class android_serialport_api_SerialPort */

#ifndef _Included_android_serialport_api_SerialPort
#define _Included_android_serialport_api_SerialPort
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     android_serialport_api_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL
        Java_com_konai_appmeter_driver_serialport_SerialPort_open
(JNIEnv *env, jobject instance, jstring path_, jint baudrate, jint flags);
/*
 * Class:     android_serialport_api_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_konai_appmeter_driver_serialport_SerialPort_close
(JNIEnv *, jobject);



#ifdef __cplusplus
}
#endif
#endif
