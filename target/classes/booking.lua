--库存id
local stockId=ARGV[1]
--用户id
local userId=ARGV[2]
--数据key
--库存key
local stockKey='order:stock:' .. stockId
--订单key
local orderKey='booking:order:' .. stockId

--脚本业务
local stock = redis.call('get', stockKey)
if(stock == false or stock == nil) then
    -- 库存key不存在，说明未初始化或已售罄
    return 3
end
if(tonumber(redis.call('get',stockKey)) <=0) then
    --库存不足
    return 1
end
--判断用户是否下过单
if(redis.call('sismember',orderKey,userId)==1)then
    --存在 重复下单
    return 2
end
--扣库存
redis.call('incrby',stockKey,-1)
redis.call('sadd',orderKey,userId)
return 0









