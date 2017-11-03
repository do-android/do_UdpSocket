package dotest.module.do_UdpSocket.activity;

import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import core.DoServiceContainer;
import core.object.DoInvokeResult;
import doext.module.do_UdpSocket.implement.do_UdpSocket_Model;
import dotest.module.do_UdpSocket.debug.DoService;

/**
 * webview组件测试样例
 */
public class WebViewSampleTestActivty extends DoTestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initModuleModel() throws Exception {
        this.model = new do_UdpSocket_Model();
    }

    @Override
    protected void initUIView() throws Exception {

    }

    @Override
    public void doTestProperties(View view) {

    }

    @Override
    protected void doTestSyncMethod() {

    }

    @Override
    protected void doTestAsyncMethod() {
        Map<String, String> _paras_loadString = new HashMap<String, String>();
        _paras_loadString.put("type", "");
        _paras_loadString.put("content", "aaaa");
        DoService.asyncMethod(this.model, "send", _paras_loadString, new DoService.EventCallBack() {
            @Override
            public void eventCallBack(String _data) {// 回调函数
                DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
            }
        });
    }

    @Override
    protected void onEvent() {

    }

    @Override
    public void doTestFireEvent(View view) {
        // fire 自定义事件
        DoInvokeResult invokeResult = new DoInvokeResult(this.model.getUniqueKey());
        this.model.getEventCenter().fireEvent("_messageName", invokeResult);
    }

}
