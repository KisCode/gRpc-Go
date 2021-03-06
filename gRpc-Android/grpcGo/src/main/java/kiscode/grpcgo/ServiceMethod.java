package kiscode.grpcgo;


import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kiscode.grpcgo.annotation.GrpcAnnotaion;

/****
 * Description: 
 * Author:  keno
 * CreateDate: 2020/11/17 21:46
 */

public final class ServiceMethod<T> {
    private static final String TAG = "ServiceMethod";
    final GrpcGo grpcGo;
    //方法返回值类型
    final Type responseType;
    //被注解方法
    final Method method;
    //方法grpc注解
    final GrpcAnnotaion methodGrpcAnnotation;
    //        final Annotation[][] parameterAnnotationsArray;
    //参数类型数组
    final Type[] parameterTypes;

    final String baseUrl;

    //
    final boolean useTransportSecurity;

    final HeaderFactory headerFactory;

    CallAdapter<?> callAdapter;

    private ServiceMethod(Builder builder) {
        this.grpcGo = builder.grpcGo;
        this.callAdapter = builder.callAdapter;
        this.responseType = builder.responseType;
        this.method = builder.method;
        this.methodGrpcAnnotation = builder.methodGrpcAnnotation;
        this.parameterTypes = builder.parameterTypes;
        this.baseUrl = builder.grpcGo.getBaseUrl();
        this.useTransportSecurity = builder.grpcGo.useTransportSecurity();
        this.headerFactory = builder.grpcGo.getHeaderFactory();
    }

    public T invoke(Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        GrpcAnnotaion annotation = methodGrpcAnnotation;
        Class grpcServiceClass = annotation.className();
        String staticMethod = "newBlockingStub";
        Channel channel = createChannel();
        //工厂方法 获取Channel
        Method newBlockingStubMethod = grpcServiceClass.getMethod(staticMethod, Channel.class);
        final Object stub = newBlockingStubMethod.invoke(null, channel);
        String methodName = annotation.methodName();
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        final Method realMethod = stub.getClass().getMethod(methodName, parameterTypes);
        Log.i(TAG, "realMethod:" + realMethod.getName() + ",returnType:" + realMethod.getGenericReturnType());

        T result = (T) realMethod.invoke(stub, args);
        if (channel instanceof ManagedChannel) {
            //调用完成后关闭通道
            ((ManagedChannel) channel).shutdown();
        }
        return result;
    }

    public Channel createChannel() {
        return getChannel(baseUrl, headerFactory.createHeaders());
    }

    private Channel getChannel(String baseUrl, Map<String, String> headers) {
        ManagedChannelBuilder managedChannelBuilder = ManagedChannelBuilder.forTarget(baseUrl);
        if (grpcGo.useTransportSecurity()) {
            managedChannelBuilder.useTransportSecurity();
        } else {
            managedChannelBuilder.usePlaintext();
        }
        //抽象方法
        HeaderClientInterceptor headerClientInterceptor = new HeaderClientInterceptor(headers);
        return ClientInterceptors.intercept(managedChannelBuilder.build(), headerClientInterceptor);
    }

    public static class Builder<T> {
        final GrpcGo grpcGo;
        //被注解方法
        final Method method;
        //方法grpc注解
        final GrpcAnnotaion methodGrpcAnnotation;
        //        final Annotation[][] parameterAnnotationsArray;
        //参数类型数组
        final Type[] parameterTypes;
        //方法返回值类型
        private Type responseType;
        private CallAdapter<?> callAdapter;

        public Builder(GrpcGo grpcGo, Method method) {
            this.grpcGo = grpcGo;
            this.method = method;
            this.methodGrpcAnnotation = method.getAnnotation(GrpcAnnotaion.class);
            this.parameterTypes = method.getParameterTypes();
            this.responseType = method.getGenericReturnType();
        }

        public ServiceMethod build() {
            callAdapter = createCallAdapter();

            if (methodGrpcAnnotation == null) {
//                throw new IllegalArgumentException("方法未被GrpcAnnotaion注解");
                throw new IllegalArgumentException("API method not found GrpcAnnotaion annotaion");
            }
            return new ServiceMethod(this);
        }

        private CallAdapter<?> createCallAdapter() {
            Type returnType = method.getGenericReturnType();
            Annotation[] annotations = method.getAnnotations();
            if (Utils.hasUnresolvableType(returnType)) {
                throw methodError(
                        "Method return type must not include a type variable or wildcard: %s", returnType);
            }
            if (returnType == void.class) {
                throw methodError("Service methods cannot return void.");
            }

            try {
                return grpcGo.callAdapter(returnType, annotations);
            } catch (RuntimeException e) {
                // Wide exception range because factories are user code.
                throw methodError(e, "Unable to create call adapter for %s", returnType);
            }
        }


        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            message = String.format(message, args);
            return new IllegalArgumentException(message
                    + "\n    for method "
                    + method.getDeclaringClass().getSimpleName()
                    + "."
                    + method.getName(), cause);
        }
    }
}
