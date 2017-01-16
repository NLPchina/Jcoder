package org.nlpcn.jcoder.server.rpc;

import java.util.Map;

import org.nlpcn.jcoder.server.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.util.Restful;

public interface RpcFilter {

	Restful match(RpcRequest req);
}
