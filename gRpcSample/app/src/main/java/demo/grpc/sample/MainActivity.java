package demo.grpc.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import demo.grpc.sample.api.GRpcApi;
import demo.grpc.sample.core.RPCMananger;
import demo.grpc.sample.core.RpcRequest;
import demo.grpc.sample.interceptor.HeaderClientInterceptor;
import grpc.sample.UserReq;
import grpc.sample.UserResp;
import grpc.sample.UserServiceGrpc;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/***
 * https://github.com/xuexiangjys/Protobuf-gRPC-Android/blob/master/app/src/main/java/com/xuexiang/protobufdemo/grpc/HttpsUtils.java
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";


    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compositeDisposable = new CompositeDisposable();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
            compositeDisposable = null;
        }
    }

    private void initView() {
        findViewById(R.id.btn_request).setOnClickListener(this);
        findViewById(R.id.btn_request_authorize).setOnClickListener(this);
        findViewById(R.id.btn_request_signle_stream).setOnClickListener(this);
        findViewById(R.id.btn_request_authorize_rxjava).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_request:
                requestGRPC();
                break;
            case R.id.btn_request_authorize:
                requestAuthorize();
                break;
            case R.id.btn_request_signle_stream:
                requestSingleStream();
                break;
            case R.id.btn_request_authorize_rxjava:
//                requestAuthorizeRxjava();
                test();
                break;
        }
    }

    private void requestGRPC() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ManagedChannel channel = ManagedChannelBuilder.forTarget("grpctest.test.rlair.net")
                        .useTransportSecurity()
                        .build();
                UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
                UserReq userReq = UserReq.newBuilder().setName("Android").build();
                try {
                    UserResp response = stub.getUser(userReq);
                    Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                    Log.i(TAG, response.getName() + "\t" + response.toString());
                } catch (StatusRuntimeException ex) {
                    Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                    Status status = ex.getStatus();
                    if (status.getCode() == Status.Code.UNAUTHENTICATED) {
                        Log.i(TAG, "请求未授权：UNAUTHENTICATED");
                    } else {
                        Log.e(TAG, ex.toString());
                    }
                }
            }
        }).start();
    }

    /***
     * 授权登录，通过
     */
    private void requestAuthorize() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ManagedChannel managedChannel = ManagedChannelBuilder.forTarget("grpctest.test.rlair.net")
                        .useTransportSecurity()
                        .build();
                HeaderClientInterceptor headerClientInterceptor = new HeaderClientInterceptor("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJFZmIiLCJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoibGlob25namlhbmciLCJqdGkiOiJjNGUxMmIwOS00MGU4LTQ0YmQtYWU2MC01MWNlYjBlNjM4ZDAiLCJpYXQiOiIxNjA1MDYzODM1IiwibmJmIjoxNjA1MDYzODM1LCJleHAiOjE2MDUxMDcwMzUsImlzcyI6ImVmYi5ybGFpci5uZXQiLCJhdWQiOiJFZmIifQ.09Iqs031U4XdkaXzsKXI41JrVmepCicfgKWX9H_Fy0f1PMnJKb8TkI1LS8Z0sDKqRRNE4uoYDk1dP1hHJpvFEEmTczsufc8b6dOXrAz5t1cJ5-SUJvaLf_6ZapDSkg7GHm0OcODYBu9jxKNVj0KjbUAQu_Q8Pbu_x-ETScFRtqiTwhTehwmQ_2oZe_426Q9tf79P4xBoFOlcT0pxXGJ8ViVX_7cIugaTxFpGuFiMp7rREOoj-qBkMxMdpHmxQnYeWN0tTvPSEBBXV10an7GwRULxMn7StLqX-diSJTQkivOJCB_G-7cRelQCjhp9zrUj2gcucHFeIlhtgEECHV00Lw");
                Channel channel = ClientInterceptors.intercept(managedChannel, headerClientInterceptor);
                UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
                UserReq userReq = UserReq.newBuilder().setName("Android").build();
                try {
                    UserResp response = stub.getUser(userReq);
                    Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                    Log.i(TAG, response.getName() + "\t" + response.toString());
                } catch (StatusRuntimeException ex) {
                    Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                    Status status = ex.getStatus();
                    if (status.getCode() == Status.Code.UNAUTHENTICATED) {
                        Log.i(TAG, "请求未授权：UNAUTHENTICATED");
                    } else {
                        Log.e(TAG, ex.toString());
                    }
                }
            }
        }).start();
    }


    /***
     * 授权登录，通过
     */
    private void requestAuthorizeRxjava() {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<UserResp>() {
            @Override
            public void subscribe(ObservableEmitter<UserResp> emitter) throws Exception {
                Log.i(TAG, "subscribe in:" + Thread.currentThread().getName());
                ManagedChannel managedChannel = ManagedChannelBuilder.forTarget("grpctest.test.rlair.net")
                        .useTransportSecurity()
                        .build();
                HeaderClientInterceptor headerClientInterceptor = new HeaderClientInterceptor("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJFZmIiLCJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoibGlob25namlhbmciLCJqdGkiOiJjNGUxMmIwOS00MGU4LTQ0YmQtYWU2MC01MWNlYjBlNjM4ZDAiLCJpYXQiOiIxNjA1MDYzODM1IiwibmJmIjoxNjA1MDYzODM1LCJleHAiOjE2MDUxMDcwMzUsImlzcyI6ImVmYi5ybGFpci5uZXQiLCJhdWQiOiJFZmIifQ.09Iqs031U4XdkaXzsKXI41JrVmepCicfgKWX9H_Fy0f1PMnJKb8TkI1LS8Z0sDKqRRNE4uoYDk1dP1hHJpvFEEmTczsufc8b6dOXrAz5t1cJ5-SUJvaLf_6ZapDSkg7GHm0OcODYBu9jxKNVj0KjbUAQu_Q8Pbu_x-ETScFRtqiTwhTehwmQ_2oZe_426Q9tf79P4xBoFOlcT0pxXGJ8ViVX_7cIugaTxFpGuFiMp7rREOoj-qBkMxMdpHmxQnYeWN0tTvPSEBBXV10an7GwRULxMn7StLqX-diSJTQkivOJCB_G-7cRelQCjhp9zrUj2gcucHFeIlhtgEECHV00Lw");
                Channel channel = ClientInterceptors.intercept(managedChannel, headerClientInterceptor);
                UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
                UserReq userReq = UserReq.newBuilder().setName("Android").build();
                try {
                    UserResp response = stub.getUser(userReq);
                    emitter.onNext(response);
                } catch (Exception ex) {
                    emitter.onError(ex);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UserResp>() {
                    @Override
                    public void accept(UserResp userResp) {
                        Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                        Log.i(TAG, userResp.getName() + "\t" + userResp.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                        if (throwable instanceof StatusRuntimeException) {
                            Status status = ((StatusRuntimeException) throwable).getStatus();
                            if (status.getCode() == Status.Code.UNAUTHENTICATED) {
                                Log.i(TAG, "请求未授权：UNAUTHENTICATED");
                            } else {
                                Log.e(TAG, throwable.toString());
                            }
                        } else {
                            Log.e(TAG, throwable.toString());
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }

    /***
     * 授权登录，通过
     */
    private void requestAuthorizeRxjava2() {
        UserReq userReq = UserReq.newBuilder().setName("Android").build();
        Disposable disposable = RpcRequest.getUser(userReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UserResp>() {
                    @Override
                    public void accept(UserResp userResp) {
                        Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                        Log.i(TAG, userResp.getName() + "\t" + userResp.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.i(TAG, "requestGRPC in:" + Thread.currentThread().getName());
                        if (throwable instanceof StatusRuntimeException) {
                            Status status = ((StatusRuntimeException) throwable).getStatus();
                            if (status.getCode() == Status.Code.UNAUTHENTICATED) {
                                Log.i(TAG, "请求未授权：UNAUTHENTICATED");
                            } else {
                                Log.e(TAG, throwable.toString());
                            }
                        } else {
                            Log.e(TAG, throwable.toString());
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }

    //异步访问
    private void requestAsycGRPC() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("grpctest.test.rlair.net")
                .useTransportSecurity()
                .build();
        UserServiceGrpc.UserServiceStub stub = UserServiceGrpc.newStub(channel);
        UserReq userReq = UserReq.newBuilder().setName("Android").build();
        stub.getUser(userReq, new StreamObserver<UserResp>() {
            @Override
            public void onNext(UserResp response) {
                Log.i(TAG, "onNext in:" + Thread.currentThread().getName());
                Log.i(TAG, response.getName() + "\t" + response.toString());
                Toast.makeText(MainActivity.this, "onNext", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable t) {
                Log.i(TAG, "onError in:" + Thread.currentThread().getName());
                Log.e(TAG, "onError:" + t);
            }

            @Override
            public void onCompleted() {
                channel.shutdown();// 关闭
            }
        });
    }


    private void requestSingleStream() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forTarget("grpctest.test.rlair.net")
                .useTransportSecurity()
                .build();
        HeaderClientInterceptor headerClientInterceptor = new HeaderClientInterceptor("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJFZmIiLCJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1lIjoibGlob25namlhbmciLCJqdGkiOiJjNGUxMmIwOS00MGU4LTQ0YmQtYWU2MC01MWNlYjBlNjM4ZDAiLCJpYXQiOiIxNjA1MDYzODM1IiwibmJmIjoxNjA1MDYzODM1LCJleHAiOjE2MDUxMDcwMzUsImlzcyI6ImVmYi5ybGFpci5uZXQiLCJhdWQiOiJFZmIifQ.09Iqs031U4XdkaXzsKXI41JrVmepCicfgKWX9H_Fy0f1PMnJKb8TkI1LS8Z0sDKqRRNE4uoYDk1dP1hHJpvFEEmTczsufc8b6dOXrAz5t1cJ5-SUJvaLf_6ZapDSkg7GHm0OcODYBu9jxKNVj0KjbUAQu_Q8Pbu_x-ETScFRtqiTwhTehwmQ_2oZe_426Q9tf79P4xBoFOlcT0pxXGJ8ViVX_7cIugaTxFpGuFiMp7rREOoj-qBkMxMdpHmxQnYeWN0tTvPSEBBXV10an7GwRULxMn7StLqX-diSJTQkivOJCB_G-7cRelQCjhp9zrUj2gcucHFeIlhtgEECHV00Lw");
        Channel channel = ClientInterceptors.intercept(managedChannel, headerClientInterceptor);
        UserServiceGrpc.UserServiceStub stub = UserServiceGrpc.newStub(channel);

        StreamObserver<UserResp> req = new StreamObserver<UserResp>() {
            @Override
            public void onNext(UserResp value) {
                Log.i(TAG, "onNext:" + value);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "onError:" + t);
            }

            @Override
            public void onCompleted() {
                Log.e(TAG, "onCompleted");
            }
        };

        StreamObserver<UserReq> streamObserver = stub.test(req);
        UserReq userDataReq = UserReq.newBuilder().setName("Android").build();
        streamObserver.onNext(userDataReq);
        UserReq userDataReq1 = UserReq.newBuilder().setName("Java").build();
        streamObserver.onNext(userDataReq1);

        streamObserver.onCompleted();
    }

    private void test() {/*
        GRpcApi rpcRequest = (GRpcApi) Proxy.newProxyInstance(GRpcApi.class.getClassLoader(), new Class[]{GRpcApi.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Log.i(TAG, proxy.getClass() + "\t" + args.length + "\t" + args[0].toString());
                // 1. invoke执行方法
                //2. 根据返回值类型进行转换

                //获取该方法上的注解
                GrpcAnnotaion annotation = method.getAnnotation(GrpcAnnotaion.class);
                Log.i(TAG, method.getName() + "\t" + annotation.className() + "." + annotation.methodName());

                Class aClass = annotation.className();
                String staticMethod = "newBlockingStub";
                Method newBlockingStubMethod = aClass.getMethod(staticMethod, Channel.class);
                Object stub = newBlockingStubMethod.invoke(null, RPCMananger.getChannel());
                Log.i(TAG, "newBlockingStub:" + stub.getClass());

                String methodName = annotation.methodName();
                Class<?>[] parameterTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    parameterTypes[i] = args[i].getClass();
                    Log.i(TAG, "parameterTypes:" + parameterTypes[i]);
                }
                Method realMethod = stub.getClass().getMethod(methodName, parameterTypes);
                Log.i(TAG, "realMethod:" + realMethod.getName());
                return realMethod.invoke(stub, args);
            }
        });

        UserReq userReq = UserReq.newBuilder().setName("Android").build();
        UserResp userResp = rpcRequest.getUser(userReq);
        Log.i(TAG, "success ：" + userResp.getName());*/

        //初始化请求参数
        UserReq userReq = UserReq.newBuilder().setName("Android").build();
        //发起请求
        UserResp userResp = RPCMananger.create(GRpcApi.class).getUser(userReq);
        Log.i(TAG, "success ：" + userResp.getName());
    }
}