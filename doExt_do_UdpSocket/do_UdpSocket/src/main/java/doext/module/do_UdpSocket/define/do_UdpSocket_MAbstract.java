package doext.module.do_UdpSocket.define;

import core.object.DoMultitonModule;
import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;


public abstract class do_UdpSocket_MAbstract extends DoMultitonModule{

	protected do_UdpSocket_MAbstract() throws Exception {
		super();
	}
	
	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception{
        super.onInit();
        //注册属性
		this.registProperty(new DoProperty("localPort", PropertyDataType.String, "", false));
		this.registProperty(new DoProperty("serverIP", PropertyDataType.String, "", false));
		this.registProperty(new DoProperty("serverPort", PropertyDataType.String, "", false));
	}
}