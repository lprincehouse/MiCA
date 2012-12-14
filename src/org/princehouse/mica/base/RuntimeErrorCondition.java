package org.princehouse.mica.base;

public enum RuntimeErrorCondition {
	PREUDPATE_EXCEPTION,
	POSTUDPATE_EXCEPTION,
	BIND_ADDRESS_EXCEPTION,
	SELF_GOSSIP,
	NULL_SELECT, 
	OPEN_CONNECTION_FAIL,
	ACTIVE_GOSSIP_EXCEPTION,
	INITIATOR_LOCK_TIMEOUT,
	SELECT_EXCEPTION,
	GOSSIP_IO_ERROR,
	MISC_INTERNAL_ERROR,
	UPDATE_EXCEPTION,
	INTERRUPTED,
	RATE_EXCEPTION,
}
