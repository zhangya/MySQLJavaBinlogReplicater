package cn.ce.binlog.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import cn.ce.binlog.mysql.parse.MysqlConnector;
import cn.ce.cons.Const;
import cn.ce.utils.common.ProFileUtil;
import cn.ce.utils.mail.Alarm;
import cn.ce.web.rest.vo.BinParseResultVO;

public class BinlogPBootStrap implements InitializingBean, DisposableBean {

	private final static Log logger = LogFactory.getLog(BinlogPBootStrap.class);

	private static MysqlConnector c;
	private String serverhost;
	private String serverPort;
	private String username;
	private String password;

	private static final BinlogParseSession bps = new BinlogParseSession();

	public void afterPropertiesSet() {
		BinParseResultVO resVo = new BinParseResultVO();
		String slaveId = "";
		try {
			slaveId = ProFileUtil.findMsgString(Const.sysconfigFileClasspath,
					"bootstrap.mysql.master.slaveid");
			this.initTarget();
			this.contiParsebinlog(resVo, slaveId);
			BinlogParserManager.save2file(c, slaveId, bps, resVo);
		} catch (Throwable e) {
			System.out.println("-----------xml binlog解析出现异常---------------");
			String err = e.getMessage();
			e.printStackTrace();
			c.disconnect();
			BinlogParserManager.sessionMap.remove(slaveId);

			err = "解析binlog线程停止，原因:" + err;
			Alarm.sendAlarmEmail(Const.sysconfigFileClasspath, err,
					resVo.toString());
		}
	}

	private void initTarget() throws Exception {
		serverhost = ProFileUtil.findMsgString(Const.sysconfigFileClasspath,
				"bootstrap.mysql.master.ip");
		serverPort = ProFileUtil.findMsgString(Const.sysconfigFileClasspath,
				"bootstrap.mysql.master.port");
		username = ProFileUtil.findMsgString(Const.sysconfigFileClasspath,
				"bootstrap.mysql.master.user");
		password = ProFileUtil.findMsgString(Const.sysconfigFileClasspath,
				"bootstrap.mysql.master.pass");
	}

	private void contiParsebinlog(BinParseResultVO resVo, String slaveId)
			throws Throwable {
		String posFileAbspath = ProFileUtil.findMsgString(
				Const.sysconfigFileClasspath,
				"binlogparse.checkpoint.fullpath.file");
		String binlogfilename = ProFileUtil.getValueFromProAbsPath(
				posFileAbspath, serverhost + ".filename");
		String binlogPosition = ProFileUtil.getValueFromProAbsPath(
				posFileAbspath, serverhost + ".pos");
		c = new MysqlConnector(serverhost, new Integer(serverPort), username,
				password);
		BinlogParserManager.startDumpToSession(new Long(slaveId),
				binlogfilename, binlogPosition, c, bps, resVo);

	}

	public static void main(String[] args) {
		try {
			BinlogPBootStrap bs = new BinlogPBootStrap();
			bs.afterPropertiesSet();
			System.out.println("-----------OVER---------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy() throws Exception {
		System.out.println("-----------Spring容器销毁---------------");
		// c.disconnect();
	}

}
