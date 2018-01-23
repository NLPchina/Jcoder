package org.nlpcn.jcoder.run.rpc;

import org.nlpcn.jcoder.run.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.util.Restful;

public interface RpcFilter {
	Restful match(RpcRequest req);
}
