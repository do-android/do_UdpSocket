package doext.module.do_UdpSocket.implement;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import doext.module.do_UdpSocket.define.do_UdpSocket_IMethod;
import doext.module.do_UdpSocket.define.do_UdpSocket_MAbstract;

/**
 * 自定义扩展MM组件Model实现，继承do_UdpSocket_MAbstract抽象类，并实现do_UdpSocket_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_UdpSocket_Model extends do_UdpSocket_MAbstract implements do_UdpSocket_IMethod {

    public do_UdpSocket_Model() throws Exception {
        super();
    }

    UDPClient client;

    @Override
    public void onPropertiesChanged(Map<String, String> _changedValues) throws Exception {

    }

    /**
     * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
     *
     * @_methodName 方法名称
     * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_invokeResult 用于返回方法结果对象
     */
    @Override
    public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        if ("close".equals(_methodName)) {
            this.close(_dictParas, _scriptEngine, _invokeResult);
            return true;
        }
        if ("open".equals(_methodName)) {
            this.open(_dictParas, _scriptEngine, _invokeResult);
            return true;
        }
        return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
    }

    /**
     * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
     *
     * @_methodName 方法名称
     * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
     * @_scriptEngine 当前page JS上下文环境
     * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
     * _scriptEngine.callback(_callbackFuncName,
     * _invokeResult);
     * 参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
     * 获取DoInvokeResult对象方式new
     * DoInvokeResult(this.getUniqueKey());
     */
    @Override
    public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
        if ("send".equals(_methodName)) {
            this.send(_dictParas, _scriptEngine, _callbackFuncName);
            return true;
        }
        return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
    }

    /**
     * 关闭连接；
     *
     * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_invokeResult 用于返回方法结果对象
     */
    @Override
    public void close(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        if (client != null) {
            client.doDispose();
            client = null;
        }
    }

    /**
     * 连接；
     *
     * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_callbackFuncName 回调函数名
     */
    DoIScriptEngine scriptEngine;
    String callbackFuncName;

    public void callBack(boolean result) {
        DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
        _invokeResult.setResultBoolean(result);
        scriptEngine.callback(callbackFuncName, _invokeResult);
    }

    /**
     * 发送数据；
     *
     * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
     * @_scriptEngine 当前Page JS上下文环境对象
     * @_callbackFuncName 回调函数名
     */
    @Override
    public void send(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
        if (client != null) {
            final String _content = DoJsonHelper.getString(_dictParas, "content", "");
            final String _type = DoJsonHelper.getString(_dictParas, "type", "");
            scriptEngine = _scriptEngine;
            callbackFuncName = _callbackFuncName;
            final String serverIp = getPropertyValue("serverIP");
            final String serverPort = getPropertyValue("serverPort");
            if (TextUtils.isEmpty(_content) || TextUtils.isEmpty(serverIp) || TextUtils.isEmpty(serverPort)) {
                callBack(false);
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.send(_type, _content, serverIp, Integer.parseInt(serverPort), _scriptEngine);
                        callBack(true);
                    } catch (Exception e) {
                        callBack(false);
                        e.printStackTrace();
                        DoServiceContainer.getLogEngine().writeError("发送异常", e);
                    }
                }
            }).start();
        } else {
            DoServiceContainer.getLogEngine().writeError("send", new Exception("未设置客户端端口号或者已手动关闭"));
        }
    }

    @Override
    public void dispose() {
        if (client != null) {
            client.doDispose();
            client = null;
        }
    }

    @Override
    public void open(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
        if (client == null) {
            String clientPort = getPropertyValue("localPort");
            ExecutorService exec = Executors.newCachedThreadPool();
            client = new UDPClient(Integer.parseInt(clientPort));
            exec.execute(client);
            client.setReceiveListener(new UDPClient.OnReceiveListener() {
                @Override
                public void fireReceiveEvent(String msg) {
                    DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
                    _invokeResult.setResultText(msg);
                    if (getEventCenter() != null) {
                        getEventCenter().fireEvent("receive", _invokeResult);
                    }
                }
            });
        } else {
            DoServiceContainer.getLogEngine().writeError("do_UdpSocket", new Exception("socket已经打开"));
        }
    }
}