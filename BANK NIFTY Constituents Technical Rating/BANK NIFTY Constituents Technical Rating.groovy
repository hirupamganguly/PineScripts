// This source code is subject to the terms of the Mozilla Public License 2.0 at https://mozilla.org/MPL/2.0/
// © tanayroy

//@version=5
indicator("BANK NIFTY Constituents Technical Rating [tanayroy]", shorttitle='BN Heatmap',overlay=true)
//all 12 constuients of bank nifty
gp='Bank Nifty Constituents'

i_tricker_symbols_1=input.symbol('NSE:HDFCBANK',title='Symbol I',group=gp)
i_tricker_symbols_2=input.symbol('NSE:ICICIBANK',title='Symbol II',group=gp)
i_tricker_symbols_3=input.symbol('NSE:KOTAKBANK',title='Symbol III',group=gp)
i_tricker_symbols_4=input.symbol('NSE:AXISBANK',title='Symbol IV',group=gp)
i_tricker_symbols_5=input.symbol('NSE:INDUSINDBK',title='Symbol V',group=gp)
i_tricker_symbols_6=input.symbol('NSE:BANDHANBNK',title='Symbol VI',group=gp)
i_tricker_symbols_7=input.symbol('NSE:SBIN',title='Symbol VII',group=gp)
i_tricker_symbols_8=input.symbol('NSE:FEDERALBNK',title='Symbol VIII',group=gp)
i_tricker_symbols_9=input.symbol('NSE:PNB',title='Symbol IX',group=gp)
i_tricker_symbols_10=input.symbol('NSE:IDFCFIRSTB',title='Symbol X',group=gp)
i_tricker_symbols_11=input.symbol('NSE:AUBANK',title='Symbol XI',group=gp)
i_tricker_symbols_12=input.symbol('NSE:RBLBANK',title='Symbol XII',group=gp)

gp2='Timeframe'

mtf2 = input.timeframe("1D", "",  group=gp2)

colBuy = input.color(#347571, "Buy       ", group="Color Settings", inline="Buy Colors")
colStrongBuy = input.color(#347571, "", group="Color Settings", inline="Buy Colors")
colNeutral = input.color(#070d0c, "Neutral ", group="Color Settings", inline="Neutral")
colSell = input.color(#361742, "Sell     ", group="Color Settings", inline="Sell Colors")
colStrongSell = input.color(#69195b, "",  group="Color Settings", inline="Sell Colors")
tableTitleColor = input.color(#295b79, "Headers", group="Color Settings", inline="Headers")

ratingSignal = "All"
roc_length=1
//Technichal rating taken from @tradingview built in indicator
// Awesome Oscillator
AO() => 
    ta.sma(hl2, 5) - ta.sma(hl2, 34)
// Stochastic RSI
StochRSI() =>
    rsi1 = ta.rsi(close, 14)
    K = ta.sma(ta.stoch(rsi1, rsi1, rsi1, 14), 3)
    D = ta.sma(K, 3)
    [K, D]
// Ultimate Oscillator
tl() => close[1] < low ? close[1]: low
uo(ShortLen, MiddlLen, LongLen) =>
    Value1 = math.sum(ta.tr, ShortLen)
    Value2 = math.sum(ta.tr, MiddlLen)
    Value3 = math.sum(ta.tr, LongLen)
    Value4 = math.sum(close - tl(), ShortLen)
    Value5 = math.sum(close - tl(), MiddlLen)
    Value6 = math.sum(close - tl(), LongLen)
    float UO = na
    if Value1 != 0 and Value2 != 0 and Value3 != 0
        var0 = LongLen / ShortLen
        var1 = LongLen / MiddlLen
        Value7 = (Value4 / Value1) * (var0)
        Value8 = (Value5 / Value2) * (var1)
        Value9 = (Value6 / Value3)
        UO := (Value7 + Value8 + Value9) / (var0 + var1 + 1)
    UO
// Ichimoku Cloud
donchian(len) => math.avg(ta.lowest(len), ta.highest(len))
ichimoku_cloud() =>
    conversionLine = donchian(9)
    baseLine = donchian(26)
    leadLine1 = math.avg(conversionLine, baseLine)
    leadLine2 = donchian(52)
    [conversionLine, baseLine, leadLine1, leadLine2]
    
calcRatingMA(ma, src) => na(ma) or na(src) ? na : (ma == src ? 0 : ( ma < src ? 1 : -1 ))
calcRating(buy, sell) => buy ? 1 : ( sell ? -1 : 0 )
price_change_close(_src,_length)=>
    roc = 100 * (_src - _src[_length])/_src[_length]
calcRatingAll() =>
    //============== MA =================
    SMA10 = ta.sma(close, 10)
    SMA20 = ta.sma(close, 20)
    SMA30 = ta.sma(close, 30)
    SMA50 = ta.sma(close, 50)
    SMA100 = ta.sma(close, 100)
    SMA200 = ta.sma(close, 200)
    
    EMA10 = ta.ema(close, 10)
    EMA20 = ta.ema(close, 20)
    EMA30 = ta.ema(close, 30)
    EMA50 = ta.ema(close, 50)
    EMA100 = ta.ema(close, 100)
    EMA200 = ta.ema(close, 200)
    
    HullMA9 = ta.hma(close, 9)
    
    // Volume Weighted Moving Average (VWMA)
    VWMA = ta.vwma(close, 20)
    
    [IC_CLine, IC_BLine, IC_Lead1, IC_Lead2] = ichimoku_cloud()
    
    // ======= Other =============
    // Relative Strength Index, RSI
    RSI = ta.rsi(close,14)
    
    // Stochastic
    lengthStoch = 14
    smoothKStoch = 3
    smoothDStoch = 3
    kStoch = ta.sma(ta.stoch(close, high, low, lengthStoch), smoothKStoch)
    dStoch = ta.sma(kStoch, smoothDStoch)
    
    // Commodity Channel Index, CCI
    CCI = ta.cci(close, 20)
    
    // Average Directional Index
    float adxValue = na, float adxPlus = na, float adxMinus = na
    [P, M, V] = ta.dmi(14, 14)
    adxValue := V
    adxPlus := P
    adxMinus := M
    // Awesome Oscillator
    ao = AO()
    
    // Momentum
    Mom = ta.mom(close, 10)
    // Moving Average Convergence/Divergence, MACD
    [macdMACD, signalMACD, _] =ta.macd(close, 12, 26, 9)
    // Stochastic RSI
    [Stoch_RSI_K, Stoch_RSI_D] = StochRSI()
    // Williams Percent Range
    WR = ta.wpr(14)
    
    // Bull / Bear Power
    BullPower = high - ta.ema(close, 13)
    BearPower = low - ta.ema(close, 13)
    // Ultimate Oscillator
    UO = uo(7,14,28)
    if not na(UO)
        UO := UO * 100
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    PriceAvg = ta.ema(close, 50)
    DownTrend = close < PriceAvg
    UpTrend = close > PriceAvg
    // calculate trading recommendation based on SMA/EMA
    float ratingMA = 0
    float ratingMAC = 0
    
    float ratingSMA10 = na
    if not na(SMA10)
        ratingSMA10 := calcRatingMA(SMA10, close)
        ratingMA := ratingMA + ratingSMA10
        ratingMAC := ratingMAC + 1
    float ratingSMA20 = na
    if not na(SMA20)
        ratingSMA20 := calcRatingMA(SMA20, close)
        ratingMA := ratingMA + ratingSMA20
        ratingMAC := ratingMAC + 1
    float ratingSMA30 = na
    if not na(SMA30)
        ratingSMA30 := calcRatingMA(SMA30, close)
        ratingMA := ratingMA + ratingSMA30
        ratingMAC := ratingMAC + 1
    float ratingSMA50 = na
    if not na(SMA50)
        ratingSMA50 := calcRatingMA(SMA50, close)
        ratingMA := ratingMA + ratingSMA50
        ratingMAC := ratingMAC + 1
    float ratingSMA100 = na
    if not na(SMA100)
        ratingSMA100 := calcRatingMA(SMA100, close)
        ratingMA := ratingMA + ratingSMA100
        ratingMAC := ratingMAC + 1
    float ratingSMA200 = na
    if not na(SMA200)
        ratingSMA200 := calcRatingMA(SMA200, close)
        ratingMA := ratingMA + ratingSMA200
        ratingMAC := ratingMAC + 1

    float ratingEMA10 = na
    if not na(EMA10)
        ratingEMA10 := calcRatingMA(EMA10, close)
        ratingMA := ratingMA + ratingEMA10
        ratingMAC := ratingMAC + 1
    float ratingEMA20 = na
    if not na(EMA20)
        ratingEMA20 := calcRatingMA(EMA20, close)
        ratingMA := ratingMA + ratingEMA20
        ratingMAC := ratingMAC + 1
    float ratingEMA30 = na
    if not na(EMA30)
        ratingEMA30 := calcRatingMA(EMA30, close)
        ratingMA := ratingMA + ratingEMA30
        ratingMAC := ratingMAC + 1
    float ratingEMA50 = na
    if not na(EMA50)
        ratingEMA50 := calcRatingMA(EMA50, close)
        ratingMA := ratingMA + ratingEMA50
        ratingMAC := ratingMAC + 1
    float ratingEMA100 = na
    if not na(EMA100)
        ratingEMA100 := calcRatingMA(EMA100, close)
        ratingMA := ratingMA + ratingEMA100
        ratingMAC := ratingMAC + 1
    float ratingEMA200 = na
    if not na(EMA200)
        ratingEMA200 := calcRatingMA(EMA200, close)
        ratingMA := ratingMA + ratingEMA200
        ratingMAC := ratingMAC + 1

    float ratingHMA = na
    if not na(HullMA9)
        ratingHMA := calcRatingMA(HullMA9, close)
        ratingMA := ratingMA + ratingHMA
        ratingMAC := ratingMAC + 1
    
    float ratingVWMA = na
    if not na(VWMA)
        ratingVWMA := calcRatingMA(VWMA, close)
        ratingMA := ratingMA + ratingVWMA
        ratingMAC := ratingMAC + 1
    
    float ratingIC = na
    if not (na(IC_Lead1) or na(IC_Lead2) or na(close) or na(close[1]) or na(IC_BLine) or na(IC_CLine))
        ratingIC := calcRating(
         IC_Lead1 > IC_Lead2 and close > IC_Lead1 and close < IC_BLine and close[1] < IC_CLine and close > IC_CLine,
         IC_Lead2 > IC_Lead1 and close < IC_Lead2 and close > IC_BLine and close[1] > IC_CLine and close < IC_CLine)
    if not na(ratingIC)
        ratingMA := ratingMA + ratingIC
        ratingMAC := ratingMAC + 1
    
    ratingMA := ratingMAC > 0 ? ratingMA / ratingMAC : na
    
    float ratingOther = 0
    float ratingOtherC = 0

    float ratingRSI = na
    if not(na(RSI) or na(RSI[1]))
        ratingRSI := calcRating(RSI < 30 and RSI[1] < RSI, RSI > 70 and RSI[1] > RSI)
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingRSI

    float ratingStoch = na
    if not(na(kStoch) or na(dStoch) or na(kStoch[1]) or na(dStoch[1]))
        ratingStoch := calcRating(kStoch < 20 and dStoch < 20 and kStoch > dStoch and kStoch[1] < dStoch[1], kStoch > 80 and dStoch > 80 and kStoch < dStoch and kStoch[1] > dStoch[1])
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingStoch

    float ratingCCI = na
    if not(na(CCI) or na(CCI[1]))
        ratingCCI := calcRating(CCI < -100 and CCI > CCI[1], CCI > 100 and CCI < CCI[1])
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingCCI

    float ratingADX = na
    if not(na(adxValue) or na(adxPlus[1]) or na(adxMinus[1]) or na(adxPlus) or na(adxMinus))
        ratingADX := calcRating(adxValue > 20 and adxPlus[1] < adxMinus[1] and adxPlus > adxMinus, adxValue > 20 and adxPlus[1] > adxMinus[1] and adxPlus < adxMinus)
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingADX

    float ratingAO = na
    if not(na(ao) or na(ao[1]))
        ratingAO := calcRating(ta.crossover(ao,0) or (ao > 0 and ao[1] > 0 and ao > ao[1] and ao[2] > ao[1]), ta.crossunder(ao,0) or (ao < 0 and ao[1] < 0 and ao < ao[1] and ao[2] < ao[1]))
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingAO

    float ratingMOM = na
    if not(na(Mom) or na(Mom[1]))
        ratingMOM := calcRating(Mom > Mom[1], Mom < Mom[1])
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingMOM

    float ratingMACD = na
    if not(na(macdMACD) or na(signalMACD))
        ratingMACD := calcRating(macdMACD > signalMACD, macdMACD < signalMACD)
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingMACD

    float ratingStoch_RSI = na
    if not(na(DownTrend) or na(UpTrend) or na(Stoch_RSI_K) or na(Stoch_RSI_D) or na(Stoch_RSI_K[1]) or na(Stoch_RSI_D[1]))
        ratingStoch_RSI := calcRating(
         DownTrend and Stoch_RSI_K < 20 and Stoch_RSI_D < 20 and Stoch_RSI_K > Stoch_RSI_D and Stoch_RSI_K[1] < Stoch_RSI_D[1],
         UpTrend and Stoch_RSI_K > 80 and Stoch_RSI_D > 80 and Stoch_RSI_K < Stoch_RSI_D and Stoch_RSI_K[1] > Stoch_RSI_D[1])
    if not na(ratingStoch_RSI)
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingStoch_RSI
    
    float ratingWR = na
    if not(na(WR) or na(WR[1]))
        ratingWR := calcRating(WR < -80 and WR > WR[1], WR > -20 and WR < WR[1])
    if not na(ratingWR)
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingWR
    
    float ratingBBPower = na
    if not(na(UpTrend) or na(DownTrend) or na(BearPower) or na(BearPower[1]) or na(BullPower) or na(BullPower[1]))
        ratingBBPower := calcRating(
         UpTrend and BearPower < 0 and BearPower > BearPower[1],
         DownTrend and BullPower > 0 and BullPower < BullPower[1])
    if not na(ratingBBPower)
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingBBPower
    
    float ratingUO = na
    if not(na(UO))
        ratingUO := calcRating(UO > 70, UO < 30)
    if not na(ratingUO)
        ratingOtherC := ratingOtherC + 1
        ratingOther := ratingOther + ratingUO
    
    ratingOther := ratingOtherC > 0 ? ratingOther / ratingOtherC : na
    
    float ratingTotal = 0
    float ratingTotalC = 0
    if not na(ratingMA)
        ratingTotal := ratingTotal + ratingMA
        ratingTotalC := ratingTotalC + 1
    if not na(ratingOther)
        ratingTotal := ratingTotal + ratingOther
        ratingTotalC := ratingTotalC + 1
    ratingTotal := ratingTotalC > 0 ? ratingTotal / ratingTotalC : na
    pcc=price_change_close(close,roc_length)

    [close,pcc,ratingTotal, ratingOther, ratingMA]

StrongBound = 0.5
WeakBound = 0.1
    
calcRatingStatus(value) =>
    if na(value)
        "-"
    else if -StrongBound > value
        "Strong\nSell"
    else if value < -WeakBound
        "Sell   "
    else if value > StrongBound
        "Strong\nBuy "
    else if value > WeakBound
        "Buy    "
    else
        "Neutral"
f_cellBgColor(_signal) =>
    _returnColor = tableTitleColor
    if _signal == "Sell   "
        _returnColor := colSell
    else if _signal == "Strong\nSell"
        _returnColor := colStrongSell
    else if _signal == "Buy    "
        _returnColor := colBuy
    else if _signal == "Strong\nBuy "
        _returnColor := colStrongBuy
    else if _signal == "Neutral" or _signal == "-"
        _returnColor := colNeutral
    _returnColor
    
array_name=array.new_string(14)
float_weigth=array.new_float(14)
price_change_array=array.new_float(14)
price_change_array_day=array.new_float(14)
htf_mtf2_ratingTotal=array.new_string(14)
htf_mtf2_ratingOther=array.new_string(14)
htf_mtf2_ratingMA=array.new_string(14)

array.set(array_name,0,i_tricker_symbols_1)
array.set(array_name,1,i_tricker_symbols_2)
array.set(array_name,2,i_tricker_symbols_3)
array.set(array_name,3,i_tricker_symbols_4)
array.set(array_name,4,i_tricker_symbols_5)
array.set(array_name,5,i_tricker_symbols_6)
array.set(array_name,6,i_tricker_symbols_7)
array.set(array_name,7,i_tricker_symbols_8)
array.set(array_name,8,i_tricker_symbols_9)
array.set(array_name,9,i_tricker_symbols_10)
array.set(array_name,10,i_tricker_symbols_11)
array.set(array_name,11,i_tricker_symbols_12)
array.set(array_name,12,'NSE:BANKNIFTY')
array.set(array_name,13,'NSE:NIFTY')


f_weightage_stock(sec,array_name_weigth,array_name_change,pca_d,_htf_mtf2_ratingTotal,_htf_mtf2_ratingOthe,_htf_mtf2_ratingMA,pos,_roc_length)=>
    f = request.financial(sec, "FLOAT_SHARES_OUTSTANDING", "FY")
    [c,price_change,ratingTotalCurrent, ratingOtherCurrent, ratingMACurrent]= request.security(sec,mtf2,calcRatingAll())
    day_change=request.security(sec,'D',price_change_close(close,roc_length))//price_change_close(close,roc_length)
    weigth=f*c
    //price_change=((c-c[1])/c[1])*100
    array.set(array_name_weigth,pos,weigth)
    array.set(array_name_change,pos,price_change)
    array.set(_htf_mtf2_ratingTotal,pos,calcRatingStatus(ratingTotalCurrent))
    array.set(_htf_mtf2_ratingOthe,pos,calcRatingStatus(ratingOtherCurrent))
    array.set(_htf_mtf2_ratingMA,pos,calcRatingStatus(ratingMACurrent))
    array.set(pca_d,pos,day_change)

f_weightage_indices(sec,array_name_weigth,array_name_change,pca_d,_htf_mtf2_ratingTotal,_htf_mtf2_ratingOthe,_htf_mtf2_ratingMA,pos,_roc_length)=>
    f =0
    [c,price_change,ratingTotalCurrent, ratingOtherCurrent, ratingMACurrent]= request.security(sec,mtf2,calcRatingAll())
    weigth=100
    day_change=request.security(sec,'D',price_change_close(close,roc_length))
    //price_change=((c-c[1])/c[1])*100
    array.set(array_name_weigth,pos,weigth)
    array.set(array_name_change,pos,price_change)
    array.set(_htf_mtf2_ratingTotal,pos,calcRatingStatus(ratingTotalCurrent))
    array.set(_htf_mtf2_ratingOthe,pos,calcRatingStatus(ratingOtherCurrent))
    array.set(_htf_mtf2_ratingMA,pos,calcRatingStatus(ratingMACurrent))
    array.set(pca_d,pos,day_change)
    
//function to change background color
f_background_color_choice(_v)=>
    var color _return = na
    if _v<0
        _return:= color.red
    else
        _return:= #196F3D//#0a0a0a
    _return
    
f_weightage_stock(i_tricker_symbols_1,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,0,roc_length)
f_weightage_stock(i_tricker_symbols_2,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,1,roc_length)
f_weightage_stock(i_tricker_symbols_3,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,2,roc_length)
f_weightage_stock(i_tricker_symbols_4,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,3,roc_length)
f_weightage_stock(i_tricker_symbols_5,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,4,roc_length)
f_weightage_stock(i_tricker_symbols_6,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,5,roc_length)
f_weightage_stock(i_tricker_symbols_7,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,6,roc_length)
f_weightage_stock(i_tricker_symbols_8,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,7,roc_length)
f_weightage_stock(i_tricker_symbols_9,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,8,roc_length)
f_weightage_stock(i_tricker_symbols_10,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,9,roc_length)
f_weightage_stock(i_tricker_symbols_11,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,10,roc_length)
f_weightage_stock(i_tricker_symbols_12,float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,11,roc_length)
f_weightage_indices('NSE:BANKNIFTY',float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,12,roc_length)
f_weightage_indices('NSE:NIFTY',float_weigth,price_change_array,price_change_array_day,htf_mtf2_ratingTotal,htf_mtf2_ratingOther,htf_mtf2_ratingMA,13,roc_length)
sum_of_w=array.sum(float_weigth)

//Display table  
f_print_table(_table,_array_name,_float_weigth,_price_change_array)=>
    table.cell(_table,0,0,"Company",text_color=color.white,bgcolor=#0a0a0a)
    table.cell(_table,1,0,"weight",text_color=color.white,bgcolor=#0a0a0a)
    table.cell(_table,2,0,"D%",text_color=color.white,bgcolor=#0a0a0a)
    table.cell(_table,3,0,mtf2+" %",text_color=color.white,bgcolor=#0a0a0a)
    table.cell(_table,4,0,"MA "+mtf2,text_color=color.white,bgcolor=#0a0a0a)
    table.cell(_table,5,0,"OSC "+mtf2,text_color=color.white,bgcolor=#0a0a0a)
    table.cell(_table,6,0,"ALL "+mtf2,text_color=color.white,bgcolor=#0a0a0a)
    impact=0.0 
    total_w=0.0
    for a=0 to 13
        float_cap=(array.get(float_weigth,a)/sum_of_w)*100
        change_pct=array.get(price_change_array,a)
        change_pct_d=array.get(price_change_array_day,a)
        total_w:=total_w+float_cap
        impact:=impact+(float_cap*change_pct)
        row=1
        table.cell(_table,0,row+a,array.get(array_name,a),text_color=color.white,bgcolor=#0a0a0a)
        if a==12
            table.cell(_table,1,row+a,'100.0',text_color=color.white,bgcolor=#0a0a0a)
        else
            table.cell(_table,1,row+a,str.tostring(float_cap,'.0'),text_color=color.white,bgcolor=#0a0a0a)
        table.cell(_table,2,row+a,str.tostring(change_pct_d,'.00'),text_color=color.white,bgcolor=f_background_color_choice(change_pct_d))
        table.cell(_table,3,row+a,str.tostring(change_pct,'.00'),text_color=color.white,
         bgcolor=f_background_color_choice(change_pct))
        
        table.cell(_table,4,row+a,array.get(htf_mtf2_ratingMA,a),text_color=color.white,bgcolor=f_cellBgColor(array.get(htf_mtf2_ratingMA,a)))
        table.cell(_table,5,row+a,array.get(htf_mtf2_ratingOther,a),text_color=color.white,bgcolor=f_cellBgColor(array.get(htf_mtf2_ratingOther,a)))
        table.cell(_table,6,row+a,array.get(htf_mtf2_ratingTotal,a),text_color=color.white,bgcolor=f_cellBgColor(array.get(htf_mtf2_ratingTotal,a)))
    // table.cell(_table,0,13,'Impact',text_color=color.white,bgcolor=#0a0a0a)        
    // table.cell(_table,1,13,tostring(total_w,'.0'),text_color=color.white,bgcolor=#0a0a0a)  
    // table.cell(_table,2,13,tostring(impact/total_w,'.00'),text_color=color.white,
    //      bgcolor=f_background_color_choice(impact/total_w))



var table display = table.new(position.top_right, 7,15 , border_color=color.black,border_width = 2)


if barstate.islast
    f_print_table(display,array_name,float_weigth,price_change_array)
    
    




