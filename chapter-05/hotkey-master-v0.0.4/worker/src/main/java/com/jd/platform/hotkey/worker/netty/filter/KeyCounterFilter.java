package com.jd.platform.hotkey.worker.netty.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.util.StrUtil;
import com.jd.platform.hotkey.common.model.HotKeyMsg;
import com.jd.platform.hotkey.common.model.KeyCountModel;
import com.jd.platform.hotkey.common.model.typeenum.MessageType;
import com.jd.platform.hotkey.common.tool.NettyIpUtil;
import com.jd.platform.hotkey.worker.counter.KeyCountItem;
import com.jd.platform.hotkey.worker.tool.InitConstant;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.jd.platform.hotkey.worker.counter.CounterConfig.COUNTER_QUEUE;

/**
 * 对热key访问次数和总访问次数进行累计
 * @author wuweifeng wrote on 2020-6-24
 * @version 1.0
 */
@Component
@Order(4)
public class KeyCounterFilter implements INettyMsgFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 该worker放到etcd worker目录的哪个app下
     */
    @Value("${etcd.workerPath}")
    private String workerPath;

    @Override
    public boolean chain(HotKeyMsg message, ChannelHandlerContext ctx) {
        if (MessageType.REQUEST_HIT_COUNT == message.getMessageType()) {
            //设置appName
            // 如果 message 的 appName 属性为空，则设置为 workerPath
            if (StrUtil.isEmpty(message.getAppName())) {
                message.setAppName(workerPath);
            }
            // 对消息进行处理并发布到具体的延时队列中进行计数
            publishMsg(message.getAppName(), message, ctx);

            return false;
        }

        return true;
    }


    /**
     * 将接收到的 key 放入延时队列中，并在一段时间后执行累加和发送。
     * 超时时间大于 5 秒则忽略消息，将警告信息记录到日志中。
     */
    private void publishMsg(String appName, HotKeyMsg message, ChannelHandlerContext ctx) {
        List<KeyCountModel> models = message.getKeyCountModels();
        // 如果 models 为空，则直接返回
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        long timeOut = SystemClock.now() - models.get(0).getCreateTime();
        //超时5秒以上的就不处理了，因为client是每10秒发送一次，所以最迟15秒以后的就不处理了
        if (timeOut > InitConstant.timeOut + 10000) {
            logger.warn("key count timeout " + timeOut + ", from ip : " + NettyIpUtil.clientIp(ctx));
            return;
        }
        //将收到的key放入延时队列，15秒后进行累加并发送
        try {
            COUNTER_QUEUE.put(new KeyCountItem(appName, models.get(0).getCreateTime(), models));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}