package cn.ce.binlog.mysql.event;

import cn.ce.binlog.session.LogBuffer;

public final class BeginLoadQueryLogEvent extends AppendBlockLogEvent {
	public BeginLoadQueryLogEvent(BinlogEventHeader header, LogBuffer buffer,
			FormatDescriptionLogEvent descriptionEvent) {
		super(header, buffer, descriptionEvent);
	}
}
