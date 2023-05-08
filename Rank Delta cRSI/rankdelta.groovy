// This source code is subject to the terms of the Mozilla Public License 2.0 at https://mozilla.org/MPL/2.0/
// © rupamganguly

//@version=5
indicator("RupamRankDelta", shorttitle = "RupamRankDelta", overlay = true)

optionScript = input.string(title = "Data Analysis Display", defval = "BankNifty", options = ['BankNifty', 'Nifty', 'Current Stock'],tooltip = "When BankNifty or Nifty is selected, the current stock's result will be displayed at the bottom of the table. It will update as the selected stock changes on the chart.")
showCandles = input.bool(title = "Smart AI Candle Detection", defval = false,tooltip = "This tool checks the candles to determine bullish or bearish trends based on CPMA (Conceptive Price Moving Average).")
enableTblHide = input.bool(title = "Table Hide", defval = false,group='Table Position/Style')
dash_loc = input.session("Top Right","Bank Dashboard Location"  ,options=["Top Right","Bottom Right","Top Left","Bottom Left", "Middle Right","Bottom Center"]  ,group='Table Position/Style')
text_size = input.session('Small',"Dashboard Size"  ,options=["Tiny","Small","Normal","Large"]  ,group='Table Position/Style')

cell_up = input(#4caf50,'Bullish'  ,group='Table Cell Color', inline = 'Bullish')
cell_dn = input(#FF5252,'Bearish'  ,group='Table Cell Color', inline = 'Bullish')
cell_netural = input(color.gray,'Neutral'  ,group='Table Cell Color', inline = 'Bullish')
txt_col = input(color.white,'Text Color'  ,group='Table Cell Color',inline = 'Text Color' )
frm_col = input(color.black,'Frame Color'  ,group='Table Cell Color', inline = 'Text Color')

cell_transp = 10 

filterNifty = optionScript == "BankNifty" ? false : true 
enableForSpecific = optionScript == "Current Stock" ? true : false

t1 = filterNifty ? "NSE:RELIANCE" : "NSE:HDFCBANK"
t2 = filterNifty ? "NSE:HDFCBANK" : "NSE:ICICIBANK" 
t3 = filterNifty ? "NSE:INFY" : "NSE:KOTAKBANK" 
t4 = filterNifty ? "NSE:HDFC" : "NSE:AXISBANK"
t5 = filterNifty ? "NSE:ICICIBANK"  : "NSE:SBIN"
t6 = filterNifty ? "NSE:TCS" : "NSE:INDUSINDBK"
t7 = filterNifty ? "NSE:KOTAKBANK" : "NSE:PNB"
t8 = filterNifty ? "NSE:HINDUNILVR": "NSE:BANDHANBNK"
t9 = filterNifty ? "NSE:BAJFINANCE" : "NSE:FEDERALBNK"
t10 = filterNifty ? "NSE:SBIN" : "NSE:IDFCFIRSTB"
t11 = syminfo.tickerid 

cell_upColor()=>
	UPcolor =#4caf50

cell_DWNColor()=>
	UPcolor =#FF5252



getAllPrices()=>
    prevClose = nz(close[1])
    prevOpen = nz(open[1])
    prevHigh = nz(high[1])
    prevLow = nz(low[1])
    currClose = close
    currOpen = open
    currHigh = high
    currLow = low

    [prevClose, prevOpen, prevHigh, prevLow, currClose, currOpen, currHigh, currLow]    

//========== Indicators Logic
getVwapPrediction(openPrice, closePrice, bnfprice, Ex_RS)=>
    //VWAP  
    VWAP = ta.vwap(openPrice)
    trend = closePrice > VWAP ? true : false
    BuySell = trend ? "Buy" : "Sell"
    VWAP_Color = trend ? color.new( cell_upColor() ,cell_transp) : color.new( cell_DWNColor() ,cell_transp) 
    bank_rs = ta.vwap(ta.change(closePrice), 14) / ta.vwap(ta.change(bnfprice), 14) * 100 > 0 ? 1 : -1
    bank_rs := bank_rs + Ex_RS
    [VWAP_Color, BuySell, bank_rs]

getEMA_20_Prediction(closePrice,bnfprice, Ex_RS)=>
    //EMA  
    EMA = ta.ema(closePrice, 20)
    trend = closePrice > EMA ? true : false
    BuySell = trend ? "Buy" : "Sell"
    bank_rs = ta.ema(ta.change(closePrice), 14) / ta.ema(ta.change(bnfprice), 14) * 100 > 0 ? 1 : -1
    bank_rs := bank_rs + Ex_RS
    [trend, BuySell, bank_rs]

getADX_Prediction(float highPrice = high, float lowPrice = low)=>
    tr = ta.rma(ta.tr(true), 14)
    dmplus = ta.rma(math.max(highPrice - highPrice[1], 0), 17)
    dmminus = ta.rma(math.max(lowPrice[1] - lowPrice, 0), 17)
    dip = 100 * dmplus / tr
    din = 100 * dmminus / tr
    dx = 100 * math.abs(dip - din) / (dip + din)
    adx = ta.ema(dx, 14)
    
    var ADXColor = color.white
    var ADXText = ''

    if adx >= 35 and dmplus > dmminus
        ADXColor:= color.new( cell_upColor() ,cell_transp)
        ADXText:= 'Buy+++'
    else if adx >= 35 and dmplus < dmminus
        ADXColor:= color.new( cell_DWNColor() ,cell_transp)
        ADXText:= 'Sell---'    
    else if adx >= 30 and dmplus > dmminus
        ADXColor:= color.new( cell_upColor() ,cell_transp)
        ADXText:= 'Buy ++'
    else if adx >= 30 and dmplus < dmminus
        ADXColor:= color.new( cell_DWNColor() ,cell_transp)
        ADXText:= 'Sell--'
    if adx >= 25 and dmplus > dmminus
        ADXColor:= color.new( cell_upColor() ,cell_transp)
        ADXText:= 'Buy+'
    else if adx >= 25 and dmplus < dmminus
        ADXColor:= color.new( cell_DWNColor() ,cell_transp)
        ADXText:= 'Sell-'    
    else if adx < 25 and adx >= 20 and dmplus > dmminus
        ADXColor:= color.new( color.orange ,cell_transp)
        ADXText:= 'Buy'
    else if adx < 25 and adx >= 20 and dmplus < dmminus
        ADXColor:= color.new( color.orange ,cell_transp)
        ADXText:= 'Sell'    
    else if adx < 20 and adx > 15
        ADXColor:= color.new( cell_netural ,cell_transp) 
        ADXText:= '-' 
    else if adx <= 15     
        ADXColor:= color.new( cell_netural ,cell_transp) 
        ADXText:= '-'  

    [ADXColor, ADXText]

get_RSI_Prediction(float closePrice=close, float bnfprice = close, int Ex_RS = 0)=>
    //RSI  
    RSI = ta.rsi(closePrice,14)
    // Generate buy and sell signals
    buy_signal = RSI > 50  and RSI < 70 //RSI < 70 and RSI > 30 and RSI > 50
    sell_signal = RSI < 50 and RSI > 30 //and RSI < 30 and RSI < 50
    BuySell = buy_signal ? "Buy" : sell_signal ? "Sell" : '-'
    RSi_Color = buy_signal ? color.new( cell_upColor() ,cell_transp) : sell_signal ? color.new( cell_DWNColor() ,cell_transp) : color.new( cell_netural ,cell_transp) 

    switch
        RSI > 25 and RSI < 50 => 
            BuySell := 'Oversold'
            RSi_Color := color.new( color.red , 0)
        RSI > 15 and RSI < 25=> 
            BuySell := 'Oversold+'
            RSi_Color := color.new(#650808, cell_transp)
        RSI < 15=> 
            BuySell := 'Ex-Oversold'
            RSi_Color := color.new(#650808, 0)
        RSI < 75 and RSI > 50 =>
            BuySell := 'Overbought'
            RSi_Color := color.new( color.green , 0)
        RSI < 85 and RSI > 75 => 
            BuySell := 'Overbought++'
            RSi_Color := color.new(#2f59c5, cell_transp)
        RSI > 85 => 
            BuySell := 'Ex-Overbought'
            RSi_Color := color.new(#6b2fc5, 0)
    BuySell := str.tostring(RSI,'#')       
    bank_rs = ta.rsi(ta.change(closePrice), 14) / ta.rsi(ta.change(bnfprice), 14) * 100 > 0 ? 1 : -1
    bank_rs := bank_rs + Ex_RS
    [RSi_Color, BuySell, bank_rs]

get_MFI_Prediction(float hlc3Price = hlc3, float bnfprice = close,int Ex_RS = 0)=>
    //MFI 
    MFI = ta.mfi(hlc3Price, 14)
    // Generate buy and sell signals
    buy_signal =  MFI > 50  and MFI < 75
    sell_signal = MFI < 50 and MFI > 25 
    BuySell = buy_signal ? "Buy" : sell_signal ? "Sell" : '-'
    MFi_Color = buy_signal ? color.new( cell_upColor() ,cell_transp) : sell_signal ? color.new( cell_DWNColor() ,cell_transp) : color.new( cell_netural ,cell_transp) 
    
    switch
        MFI > 20 and MFI < 50 => 
            BuySell := 'Oversold'
            MFi_Color := color.new( color.red , 0)
        MFI > 10 and MFI < 20=> 
            BuySell := 'Oversold+'
            MFi_Color := color.new(#650808, cell_transp)
        MFI < 10=> 
            BuySell := 'Ex-Oversold'
            MFi_Color := color.new(#650808, 0)
        MFI < 80 and MFI > 50=>
            BuySell := 'Overbought'
            MFi_Color := color.new( color.green , 0)
        MFI > 80 and MFI < 90=> 
            BuySell := 'Overbought+'
            MFi_Color := color.new(#2f59c5, cell_transp)
        MFI > 90 => 
            BuySell := 'Ex-Overbought'
            MFi_Color := color.new(#6b2fc5, 0) 

    BuySell := str.tostring(MFI,'#')
    bank_rs = ta.mfi(ta.change(hlc3Price), 14) / ta.mfi(ta.change(bnfprice), 14) * 100 > 0 ? 1 : -1
    bank_rs := bank_rs + Ex_RS
    [MFi_Color, BuySell, bank_rs]
   
get_Alligator_Prediction(hl2Alligs, closePrice)=>
    // Alligator
    jawLen = 13
    teethLen = 8
    lipsLen = 5

    jaw = ta.sma(hl2Alligs, jawLen)
    teeth = ta.sma(hl2Alligs, teethLen)
    lips = ta.sma(hl2Alligs, lipsLen)

    buyAlligator = lips > teeth and teeth > jaw and closePrice > lips
    sellAlligator = lips < teeth and teeth < jaw and closePrice < lips
    BuySell = buyAlligator ? "Buy" : sellAlligator ? "Sell" : 'Sleep'
    Alligator_Color = buyAlligator ? color.new( cell_upColor() ,cell_transp) : sellAlligator ? color.new( cell_DWNColor() ,cell_transp) : color.new( cell_netural ,cell_transp) 
    [Alligator_Color, BuySell]


get_MACD_Prediction(closePrice)=>
// Calculate the MACD and signal line
    [macdLine, signalLine, histLine] = ta.macd(closePrice, 12, 26, 9)
    // Generate buy and sell signals
    buy_signal = macdLine > signalLine //  ta.crossover(macdLine, signalLine)
    sell_signal = macdLine < signalLine // ta.crossunder(macdLine, signalLine)
    BuySell = buy_signal ? "Buy" : sell_signal ? "Sell" : '-'
    MACD_Color = buy_signal ? color.new( cell_upColor() ,cell_transp) : sell_signal ? color.new( cell_DWNColor() ,cell_transp) : color.new( cell_netural ,cell_transp) 
    [MACD_Color, BuySell]

frama_Calculation(float src = close,int length = 21, float mult = 1.0) =>
// Define the FRAMA function using a loop
    alpha = 2 / (length + 1)
    sum_wt = 0.0
    sum_wt_src = 0.0
    for i = 0 to length - 1 by 1
        weight = math.exp(math.log(mult) * i * i / (length * length))
        sum_wt += weight
        sum_wt_src += weight * src[length - 1 - i]
        sum_wt_src
    frama_value = sum_wt_src / sum_wt
    frama_value

CSM_CPMA(simple int length=21, float price = close, float HL2 = hl2,float Open = open , float High = high, float Low = low, float OHLC4 = ohlc4, float HLC3 = hlc3, float HLCC4 = hlcc4)=>
    // Calculate the average of the last 21 candles for each price type
    price_avg = ta.ema(price, length) 
    HL2_avg =  ta.sma(HL2, length)
    Open_avg = ta.ema(Open, length) 
    High_avg =  ta.sma(High, length) 
    Low_avg = ta.ema(Low, length)
    OHLC4_avg =  ta.sma(OHLC4, length)
    HLC3_avg = ta.ema(HLC3, length) 
    HLCC4_avg = ta.sma(HLCC4, length)

    // Calculate the average of the price types
    price_average = (price_avg + HL2_avg + Open_avg + High_avg + Low_avg + OHLC4_avg + HLC3_avg + HLCC4_avg) / 8
    price_average := na(price_average[1]) ? price_average : price_average[1] + (price - price_average[1]) / (length * math.pow(price/price_average[1], 4))    

    price_average

get_FARMA_Prediction(closePrice,  bnfprice, Ex_RS)=>
    // Calculate the FRAMA
    frama_value = frama_Calculation(closePrice, 16, 1)
    // Generate buy and sell signals
    buy_signal = closePrice > frama_value //ta.crossover(closePrice, frama_value)
    sell_signal = closePrice < frama_value //ta.crossunder(closePrice, frama_value)
    BuySell = buy_signal ? "Buy" : sell_signal ? "Sell" : '-'
    FARMA_Color = buy_signal ? color.new( cell_upColor() ,cell_transp) : sell_signal ? color.new( cell_DWNColor() ,cell_transp) : color.new( cell_netural ,cell_transp) 
    bank_rs = frama_Calculation(ta.change(closePrice),16,1) / frama_Calculation(ta.change(bnfprice), 16, 1) * 100 > 0 ? 1 : -1
    bank_rs := bank_rs + Ex_RS
    [FARMA_Color, BuySell, bank_rs]

// Thanks For TradingView
// The same on Pine Script™
pine_supertrend(factor, atrPeriod, hl2Price, closePrice) =>
    src = hl2Price
    atr = ta.atr(atrPeriod)
    upperBand = src + factor * atr
    lowerBand = src - factor * atr
    prevLowerBand = nz(lowerBand[1])
    prevUpperBand = nz(upperBand[1])

    lowerBand := lowerBand > prevLowerBand or closePrice[1] < prevLowerBand ? lowerBand : prevLowerBand
    upperBand := upperBand < prevUpperBand or closePrice[1] > prevUpperBand ? upperBand : prevUpperBand
    int direction = na
    float superTrend = na
    prevSuperTrend = superTrend[1]
    if na(atr[1])
        direction := 1
    else if prevSuperTrend == prevUpperBand
        direction := closePrice > upperBand ? -1 : 1
    else
        direction := closePrice < lowerBand ? 1 : -1
    superTrend := direction == -1 ? lowerBand : upperBand
    [superTrend, direction]

get_SuperTrend_Prediction(factor, atrPeriod, hl2Price, closePrice)=>
    //Supertrend
    [superTrend, dir] = pine_supertrend(factor, atrPeriod, hl2Price, closePrice)    
    
    buySignal =  dir < 0
    sellSignal = dir > 0

    BuySell = buySignal ? "Buy" : sellSignal ? "Sell" : '-'
    ST_Color = buySignal ? color.new( cell_upColor() ,cell_transp) : sellSignal ? color.new( cell_DWNColor() ,cell_transp) : color.new( cell_netural ,cell_transp) 
    [ST_Color, BuySell]


// For first symbol
getLtp_N_Chang(float openPrice = open, float closePrice = close, float highPrice = high, float hl2Price = hl2, float lowPrice = low, float hlc3Price = hlc3,float stockLastClosePrice = close,float bankNiftyClose = close)=>
	
	ts1 = closePrice 
    ts1C = stockLastClosePrice
    ts1Chng = (ts1-ts1C)
    ts1p = (ts1-ts1C)*100/ts1C
  
    [VWAPColor, VWAPText, Vwap_bnf] = getVwapPrediction(openPrice = openPrice, closePrice = closePrice, bnfprice = bankNiftyClose, Ex_RS = 0)
    [trend, BuySell, EMA_bnf] = getEMA_20_Prediction(closePrice = closePrice, bnfprice = bankNiftyClose, Ex_RS = Vwap_bnf )
    [ADXColor, ADXText] = getADX_Prediction(highPrice = highPrice, lowPrice = lowPrice)
    [RSIColor, RSIText, RSI_bnf] = get_RSI_Prediction(closePrice = closePrice, bnfprice = bankNiftyClose, Ex_RS = EMA_bnf)
    [MFIColor, MFIText, MFI_bnf] = get_MFI_Prediction(hlc3Price = hlc3Price, bnfprice = bankNiftyClose, Ex_RS = RSI_bnf)
    [AllG_Color, AllG_Text] = get_Alligator_Prediction(hl2Alligs = hl2Price, closePrice = closePrice) 
    [MACD_Color, MACDText] = get_MACD_Prediction(closePrice = closePrice) 
    [FARMA_Color, FARMAText, FARMA_bnf] = get_FARMA_Prediction(closePrice = closePrice, bnfprice = bankNiftyClose, Ex_RS = MFI_bnf)
    [ST_Color_21, ST_Text_21] = get_SuperTrend_Prediction(factor=1, atrPeriod=21, hl2Price=hl2Price, closePrice=closePrice)
    [ST_Color_14, ST_Text_14] = get_SuperTrend_Prediction(factor=2, atrPeriod=14, hl2Price=hl2Price, closePrice=closePrice)
    [ST_Color_10, ST_Text_10] = get_SuperTrend_Prediction(factor=3, atrPeriod=10, hl2Price=hl2Price, closePrice=closePrice)

    // FARMA_bnf := ADXText == 'Buy' or ADXText == 'Buy+' or ADXText == 'Buy++' or ADXText == 'Buy+++' ? FARMA_bnf + 1 : ADXText == 'Sell' or ADXText == 'Sell-' or ADXText == 'Sell--' or ADXText == 'Sell---' ? FARMA_bnf - 1 : FARMA_bnf
    // FARMA_bnf := AllG_Text == 'Buy' ? FARMA_bnf + 1 : AllG_Text == 'Sell' ? FARMA_bnf - 1 : FARMA_bnf
    // FARMA_bnf := MACDText == 'Buy' ? FARMA_bnf + 1 : FARMA_bnf - 1
    // FARMA_bnf := ST_Text_21 == 'Buy' ? FARMA_bnf + 1 : FARMA_bnf - 1
    // FARMA_bnf := ST_Text_14 == 'Buy' ? FARMA_bnf + 1 : FARMA_bnf - 1
    // FARMA_bnf := ST_Text_10 == 'Buy' ? FARMA_bnf + 1 : FARMA_bnf - 1
    
    // Calculate the RS ratio
    rs_ratio = closePrice / bankNiftyClose

    // Calculate the moving average of the RS ratio over 20 periods
    rs_ma = ta.sma(rs_ratio, 20)
    // Plot buy/sell signals based on the RS comparison
    buy = rs_ratio > rs_ma
    sell = rs_ratio < rs_ma
    
    RS_Text = buy ? "Buy" : sell ?  "Sell" : '-'
    RS_Color = buy ? color.new( cell_upColor() ,cell_transp) : sell ? color.new( cell_DWNColor() ,cell_transp) :  color.new( cell_netural ,cell_transp)

    [ts1, ts1Chng, ts1p, VWAPColor, VWAPText, trend, BuySell, ADXColor, ADXText, RSIColor, RSIText, MFIColor, MFIText, AllG_Color, AllG_Text, MACD_Color, MACDText, FARMA_Color, FARMAText, ST_Color_21, ST_Text_21, ST_Color_14, ST_Text_14, ST_Color_10, ST_Text_10, RS_Text, RS_Color]    

//Thanks For Trading For Candlistick Pattern ditection script
//This is totaly depend on inbuilt candlistic patters script by TRADINGVIEW, i am adding few modification.
//Thanks for tradingview for candlestick pattern script,
funcGetCandlebaseConfiguration(simple int C_Len = 14,float C_ShadowPercent = 5, float C_ShadowEqualsPercent = 100, float C_DojiBodyPercent = 5,int C_Factor = 2)=>
   
    [prevClose, prevOpen, prevHigh, prevLow, currClose, currOpen, currHigh, currLow] = getAllPrices()
    
    C_BodyHi = math.max(currClose, currOpen)
    C_BodyLo = math.min(currClose, currOpen)
    C_Body = C_BodyHi - C_BodyLo
    C_BodyAvg = ta.ema(C_Body, C_Len)
    C_SmallBody = C_Body < C_BodyAvg
    C_LongBody = C_Body > C_BodyAvg
    C_UpShadow = currHigh - C_BodyHi
    C_DnShadow = C_BodyLo - currLow
    C_HasUpShadow = C_UpShadow > C_ShadowPercent / 100 * C_Body
    C_HasDnShadow = C_DnShadow > C_ShadowPercent / 100 * C_Body
    C_WhiteBody = currOpen < currClose
    C_BlackBody = currOpen > currClose
    C_Range = currHigh - currLow
    C_IsInsideBar = C_BodyHi[1] > C_BodyHi and C_BodyLo[1] < C_BodyLo
    C_BodyMiddle = C_Body / 2 + C_BodyLo
    C_ShadowEquals = C_UpShadow == C_DnShadow or math.abs(C_UpShadow - C_DnShadow) / C_DnShadow * 100 < C_ShadowEqualsPercent and math.abs(C_DnShadow - C_UpShadow) / C_UpShadow * 100 < C_ShadowEqualsPercent
    C_IsDojiBody = C_Range > 0 and C_Body <= C_Range * C_DojiBodyPercent / 100
    C_Doji = C_IsDojiBody and C_ShadowEquals 

    [C_BodyHi, C_BodyLo, C_Body, C_BodyAvg, C_SmallBody, C_LongBody, C_UpShadow, C_DnShadow, C_HasUpShadow, C_HasDnShadow, C_WhiteBody, C_BlackBody, C_Range, C_IsInsideBar, C_BodyMiddle, C_ShadowEquals, C_IsDojiBody,  C_Doji]

// Define a function to detect bullish engulfing patterns
candlepatternbullish(simple int C_Len = 14,float C_ShadowPercent = 5, float C_ShadowEqualsPercent = 100, float C_DojiBodyPercent = 5)=>

    [C_BodyHi, C_BodyLo, C_Body, C_BodyAvg, C_SmallBody, C_LongBody, C_UpShadow, C_DnShadow, C_HasUpShadow, C_HasDnShadow, C_WhiteBody, C_BlackBody, C_Range, C_IsInsideBar, C_BodyMiddle, C_ShadowEquals, C_IsDojiBody,  C_Doji] = funcGetCandlebaseConfiguration(C_Len = C_Len, C_ShadowPercent = C_ShadowPercent, C_ShadowEqualsPercent = C_ShadowEqualsPercent, C_DojiBodyPercent = C_DojiBodyPercent, C_Factor = 2)

    [prevClose, prevOpen, prevHigh, prevLow, currClose, currOpen, currHigh, currLow] = getAllPrices()

    IsBullish = prevClose < prevOpen and currClose > currOpen and currClose > prevOpen and currOpen < prevClose and currHigh > prevHigh and currLow < prevLow
    //1
    IsAlternativeBullish = prevOpen > prevClose ? currClose > currOpen ? currLow >=prevLow[1] ? currOpen <= prevClose[1] ? prevOpen - currOpen > prevOpen[1] - prevClose[1] ? true : false : false : false : false : false
    //2
    engulfing = C_WhiteBody and C_LongBody and C_BlackBody[1] and C_SmallBody[1] and currClose >= prevOpen[1] and currOpen <= prevClose[1] and (currClose > prevOpen[1] or currOpen < prevClose[1])
    //3. Rising Window
    risingWindow =  C_Range != 0 and C_Range[1] != 0 and currLow > prevHigh
    //4.  Rising 3 methods
    risingMethods = C_LongBody[4] and C_WhiteBody[4] and C_SmallBody[3] and C_BlackBody[3] and open[3] < high[4] and close[3] > low[4] and C_SmallBody[2] and C_BlackBody[2] and open[2] < high[4] and close[2] > low[4] and C_SmallBody[1] and C_BlackBody[1] and prevOpen < high[4] and prevClose > low[4] and C_LongBody and C_WhiteBody and currClose > close[4]
    //5. Up Side Tuski
    up_Tuski = C_LongBody[2] and C_SmallBody[1] and C_WhiteBody[2] and C_BodyLo[1] > C_BodyHi[2] and C_WhiteBody[1] and C_BlackBody and C_BodyLo >= C_BodyHi[2] and C_BodyLo <= C_BodyLo[1]
    //6. MarooBhuju
    C_MarubozuShadowPercentWhite = 5.0
    marubozuWhiteBullish = C_WhiteBody and C_LongBody and C_UpShadow <= C_MarubozuShadowPercentWhite / 100 * C_Body and C_DnShadow <= C_MarubozuShadowPercentWhite / 100 * C_Body and C_WhiteBody
    //7 Dragon Fly Doji
    dragonflyDojiBullish = C_IsDojiBody and C_UpShadow <= C_Body

    IsBullish := IsBullish or IsAlternativeBullish or engulfing or risingWindow or risingMethods or up_Tuski or marubozuWhiteBullish or dragonflyDojiBullish 
    
    IsBullish

// Define a function to detect bearish engulfing patterns
candlepatternbearish(simple int C_Len = 14,float C_ShadowPercent = 5, float C_ShadowEqualsPercent = 100, float C_DojiBodyPercent = 5) =>
    
    [C_BodyHi, C_BodyLo, C_Body, C_BodyAvg, C_SmallBody, C_LongBody, C_UpShadow, C_DnShadow, C_HasUpShadow, C_HasDnShadow, C_WhiteBody, C_BlackBody, C_Range, C_IsInsideBar, C_BodyMiddle, C_ShadowEquals, C_IsDojiBody,  C_Doji] = funcGetCandlebaseConfiguration(C_Len = C_Len, C_ShadowPercent = C_ShadowPercent, C_ShadowEqualsPercent = C_ShadowEqualsPercent, C_DojiBodyPercent = C_DojiBodyPercent, C_Factor = 2)

    [prevClose, prevOpen, prevHigh, prevLow, currClose, currOpen, currHigh, currLow] = getAllPrices()
    
    IsBearish = prevClose > prevOpen and currClose < currOpen and currClose < prevOpen and currOpen > prevClose and currHigh > prevHigh and currLow < prevLow

    isAlternativeBearish = prevOpen < prevClose ? currClose < currOpen ? currHigh <=prevHigh ? currOpen >= prevClose ? currOpen - currClose > prevClose - prevOpen ? true : false : false : false : false : false
    //1. Falling Window
    fallingWindow = C_Range != 0 and C_Range[1] != 0 and currHigh < prevLow
    //2. Falling Three Methods
    falling_3_Methods = C_LongBody[4] and C_BlackBody[4] and C_SmallBody[3] and C_WhiteBody[3] and open[3] > low[4] and close[3] < high[4] and C_SmallBody[2] and C_WhiteBody[2] and open[2] > low[4] and close[2] < high[4] and C_SmallBody[1] and C_WhiteBody[1] and prevOpen > low[4] and prevClose < high[4] and C_LongBody and C_BlackBody and currClose < close[4]
    //3. Down Side Tuski
    dwn_Tuski = C_LongBody[2] and C_SmallBody[1] and C_BlackBody[2] and C_BodyHi[1] < C_BodyLo[2] and C_BlackBody[1] and C_WhiteBody and C_BodyHi <= C_BodyLo[2] and C_BodyHi >= C_BodyHi[1]
    //4. MarooBhuju
    C_MarubozuShadowPercentBearish = 5.0
    marubozuBlackBearish = C_BlackBody and C_LongBody and C_UpShadow <= C_MarubozuShadowPercentBearish / 100 * C_Body and C_DnShadow <= C_MarubozuShadowPercentBearish / 100 * C_Body and C_BlackBody
    //5. Gravestone Doji
    gravestoneDojiBearish = C_IsDojiBody and C_DnShadow <= C_Body
    //6. Dark Cloud Cover
    dark_CC = C_WhiteBody[1] and C_LongBody[1] and C_BlackBody and currOpen >= prevHigh and currClose < C_BodyMiddle[1] and currClose > prevOpen

    IsBearish := IsBearish or isAlternativeBearish or fallingWindow or falling_3_Methods or dwn_Tuski or marubozuBlackBearish or gravestoneDojiBearish or dark_CC

    IsBearish


BullishCandlePatternOnDownTrend(simple int C_Len = 14,float C_ShadowPercent = 5, float C_ShadowEqualsPercent = 100, float C_DojiBodyPercent = 5,int C_Factor = 2) =>
    
    [C_BodyHi, C_BodyLo, C_Body, C_BodyAvg, C_SmallBody, C_LongBody, C_UpShadow, C_DnShadow, C_HasUpShadow, C_HasDnShadow, C_WhiteBody, C_BlackBody, C_Range, C_IsInsideBar, C_BodyMiddle, C_ShadowEquals, C_IsDojiBody,  C_Doji] = funcGetCandlebaseConfiguration(C_Len = C_Len, C_ShadowPercent = C_ShadowPercent, C_ShadowEqualsPercent = C_ShadowEqualsPercent, C_DojiBodyPercent = C_DojiBodyPercent, C_Factor = C_Factor)
    
    [prevClose, prevOpen, prevHigh, prevLow, currClose, currOpen, currHigh, currLow] = getAllPrices()

    Bullish = false
    //1
    hammer = C_SmallBody and C_Body > 0 and C_BodyLo > hl2 and C_DnShadow >= C_Factor * C_Body and not C_HasUpShadow
     //2 Bullish Hammer
    C_BullishHammer = C_LongBody and C_HasDnShadow and not C_HasUpShadow and C_WhiteBody and C_Doji and C_IsInsideBar and C_DnShadow > C_Factor * C_BodyAvg
    //3.  Tweezer Bottom
    tw_Bottom = (not C_IsDojiBody or C_HasUpShadow and C_HasDnShadow) and math.abs(currLow - prevLow) <= C_BodyAvg * 0.05 and C_BlackBody[1] and C_WhiteBody and C_LongBody[1]
    //4. Doji Star Bullish
    dj_Star = C_BlackBody[1] and C_LongBody[1] and C_IsDojiBody and C_BodyHi < C_BodyLo[1]
    //5. Morning Star Doji
    mrng_StraDoji = C_LongBody[2] and C_IsDojiBody[1] and C_LongBody and C_BlackBody[2] and C_BodyHi[1] < C_BodyLo[2] and C_WhiteBody and C_BodyHi >= C_BodyMiddle[2] and C_BodyHi < C_BodyHi[2] and C_BodyHi[1] < C_BodyLo 
    //6. Pearsing Bulish
    pearcing_Bullish = C_BlackBody[1] and C_LongBody[1] and C_WhiteBody and currOpen <= prevLow and currClose > C_BodyMiddle[1] and currClose < prevOpen
    //7. Inverted Hammer
    inver_Hammer = C_SmallBody and C_Body > 0 and C_BodyHi < hl2 and C_UpShadow >= C_Factor * C_Body and not C_HasDnShadow
    
    Bullish := hammer or C_BullishHammer or tw_Bottom or dj_Star or mrng_StraDoji or pearcing_Bullish or inver_Hammer

    Bullish


BearishCandlePatternOnUpTrend(simple int C_Len = 14,float C_ShadowPercent = 5, float C_ShadowEqualsPercent = 100, float C_DojiBodyPercent = 5,int C_Factor = 2) =>
    
    [C_BodyHi, C_BodyLo, C_Body, C_BodyAvg, C_SmallBody, C_LongBody, C_UpShadow, C_DnShadow, C_HasUpShadow, C_HasDnShadow, C_WhiteBody, C_BlackBody, C_Range, C_IsInsideBar, C_BodyMiddle, C_ShadowEquals, C_IsDojiBody,  C_Doji] = funcGetCandlebaseConfiguration(C_Len = C_Len, C_ShadowPercent = C_ShadowPercent, C_ShadowEqualsPercent = C_ShadowEqualsPercent, C_DojiBodyPercent = C_DojiBodyPercent, C_Factor = C_Factor)
    [prevClose, prevOpen, prevHigh, prevLow, currClose, currOpen, currHigh, currLow] = getAllPrices()
    //1.Tweezer Top\nTweezer Top
    dwn_tweezer = (not C_IsDojiBody or C_HasUpShadow and C_HasDnShadow) and math.abs(currHigh - prevHigh) <= C_BodyAvg * 0.05 and C_WhiteBody[1] and C_BlackBody and C_LongBody[1]
   //2. dark_cloud_cover
    dark_cloud_cover = currOpen > currClose and currClose < C_BodyMiddle[1] - 0.5 * C_BodyAvg
    //3. Evening Doji Star
    evn_Doji = C_LongBody[2] and C_IsDojiBody[1] and C_LongBody and C_WhiteBody[2] and C_BodyLo[1] > C_BodyHi[2] and C_BlackBody and C_BodyLo <= C_BodyMiddle[2] and C_BodyLo > C_BodyLo[2] and C_BodyLo[1] > C_BodyHi
    //4. Doji Star Bearish
    doji_Star = C_WhiteBody[1] and C_LongBody[1] and C_IsDojiBody and C_BodyLo > C_BodyHi[1]
    //5. Hanging Man
    hang_man = C_SmallBody and C_Body > 0 and C_BodyLo > hl2 and C_DnShadow >= C_Factor * C_Body and not C_HasUpShadow
    //6. Shooting Star
    shoot_Star = C_SmallBody and C_Body > 0 and C_BodyHi < hl2 and C_UpShadow >= C_Factor * C_Body and not C_HasDnShadow
    //7.  Evening Star
    evening_Star = C_LongBody[2] and C_SmallBody[1] and C_LongBody and C_WhiteBody[2] and C_BodyLo[1] > C_BodyHi[2] and C_BlackBody and C_BodyLo <= C_BodyMiddle[2] and C_BodyLo > C_BodyLo[2] and C_BodyLo[1] > C_BodyHi
    //8. Harami Bearish
    haramiBearish = C_LongBody[1] and C_WhiteBody[1] and C_BlackBody and C_SmallBody and currHigh <= C_BodyHi[1] and currLow >= C_BodyLo[1]


    Bearish = dwn_tweezer or dark_cloud_cover or evn_Doji or doji_Star or hang_man or shoot_Star or evening_Star or haramiBearish

    Bearish



get_EngulfimgBullish_Detection()=>
    isEngulf = open[1] > close[1] ? close > open ? high >=high[1] and low <= low[1] ? open <= close[1] ? close - open > open[1] - close[1] ? true : false : false : false : false : false

get_EngulfimgBearish_Detection()=>
    isEngulf = open[1] < close[1] ? close < open ? high >=high[1] and low <=low[1] ? open >= close[1] ? open - close > close[1] - open[1] ? true : false : false : false : false : false

// Detect bullish and bearish engulfing patterns
get_EngulfingBullish_Detection() =>
    isEngulf = open[1] > close[1] ? close > open ? high >= high[1] and low <= low[1] ? open <= close[1] ? (close - open) > (open[1] - close[1]) ? true : false : false : false : false : false

get_EngulfingBearish_Detection() =>
    isEngulf = open[1] < close[1] ? close < open ? high >= high[1] and low <= low[1] ? open >= close[1] ? (open - close) > (close[1] - open[1]) ? true : false : false : false : false : false





/// -------------- Table Start -------------------

var table_position = dash_loc == 'Top Left' ? position.top_left :
  dash_loc == 'Bottom Left' ? position.bottom_left :
  dash_loc == 'Middle Right' ? position.middle_right :
  dash_loc == 'Bottom Center' ? position.bottom_center :
  dash_loc == 'Top Right' ? position.top_right : position.bottom_right
  
var table_text_size = text_size == 'Tiny' ? size.tiny :
  text_size == 'Small' ? size.small :
  text_size == 'Normal' ? size.normal : size.large

///// -- Table Inputs
max = 120
min = 10

var t = table.new(table_position,19,math.abs(max-min)+2,
  bgcolor =color.rgb(86, 89, 101),
  frame_color=frm_col,
  frame_width=1,
  border_color=frm_col,
  border_width=1)
   
cpmaAVG = close
get_BankComponent_Details(symbol)=>
    
    [lastClosePrice, lastOpenPrice] = request.security(symbol, "1D", [close[1],open[1]])
    [openPrice,closePrice,highPrice,hl2Price,lowPrice,hlc3Price,OHLC4Price,hlcc4Price, vol] = request.security(symbol, timeframe.period, [open,close,high,hl2,low, hlc3, ohlc4,hlcc4,volume])
    filterBy = enableForSpecific ? syminfo.tickerid : filterNifty ? "NSE:NIFTY" : "NSE:BANKNIFTY"
    [bankNiftyClose, prevBankNiftyClose] = request.security(filterBy, "1D", [close,close[1]])

    priceVal = CSM_CPMA(length = 14 ,price = closePrice,HL2 = hl2Price, Open = openPrice , High = highPrice, Low = lowPrice, OHLC4 = OHLC4Price, HLC3 = hlc3Price, HLCC4 = hlcc4Price)

    csm_cpmaBuy = closePrice > priceVal 
    csm_cpmaSell =  closePrice < priceVal
    csm_cpmaBuy_text = csm_cpmaBuy ? 'Buy' : 'Sell'

    index_change = (bankNiftyClose / prevBankNiftyClose) -1
    stock_change = (closePrice / lastClosePrice) - 1
    w_r_t_Result = stock_change > 0 and csm_cpmaBuy ? 'Buy' : stock_change < 0  and csm_cpmaSell ? 'Sell' : 'Neutral'
   
    [ts1, ts1Chng, ts1p, VWAPColor, VWAPText, trend, BuySell, ADXColor, ADXText, RSIColor, RSIText, MFIColor, MFIText, AllG_Color, AllG_Text, MACD_Color, MACDText, FARMA_Color, FARMAText, ST_Color_21, ST_Text_21, ST_Color_14, ST_Text_14, ST_Color_10, ST_Text_10, RS_Text, RS_Color] = getLtp_N_Chang(openPrice, closePrice , highPrice , hl2Price, lowPrice, hlc3Price, lastClosePrice, bankNiftyClose)
    
    // trendUp = VWAPText == 'Buy' and csm_cpmaBuy and MACDText == 'Buy' and FARMAText == 'Buy' ? true : false
    // trendDwn =  VWAPText == 'Sell' and csm_cpmaSell and MACDText == 'Sell' and FARMAText == 'Sell' ? true : false
    
    w_r_t_Result := w_r_t_Result == 'Buy' and csm_cpmaBuy ? 'Buy' : w_r_t_Result == 'Sell' and csm_cpmaSell ? 'Sell' : 'Neutral'

    [w_r_t_Result,ts1, ts1Chng, vol, VWAPColor, VWAPText, csm_cpmaBuy, csm_cpmaBuy_text, ADXColor, ADXText, RSIColor, RSIText, MFIColor, MFIText, AllG_Color, AllG_Text, MACD_Color, MACDText, FARMA_Color, FARMAText, ST_Color_21, ST_Text_21, ST_Color_14, ST_Text_14, ST_Color_10, ST_Text_10, RS_Text, RS_Color, priceVal]



//Confirmation From RSI, MFI, FRAMA, MACD and VWAP
[w_r_t_Result, ltp, ltpCng, ltpPercChng, srcpVwap, srcpVWAPText, srcpCPMA, srcpCPMAtext, srcpADXColor, srcpADXText, srcpRSIColor, srcpRSIText, srcpMFIColor, srcpMFIText, srcpAllG_Color, srcpAllG_Text, srcpMACD_Color, srcpMACDText, srcpFARMA_Color, srcpFARMAText, srcpST_Color_21, srcpST_Text_21, srcpST_Color_14, srcpST_Text_14, srcpST_Color_10, srcpST_Text_10, srcpRS_Text, srcpRS_Color, CPMA1]   = get_BankComponent_Details(syminfo.tickerid)

trendUp = srcpVWAPText == 'Buy' and srcpCPMAtext == 'Buy' and srcpMACDText == 'Buy' and srcpFARMAText == 'Buy' ? true : false
trendDwn =  srcpVWAPText == 'Sell' and srcpCPMAtext == 'Sell' and srcpMACDText == 'Sell' and srcpFARMAText == 'Sell' ? true : false

CPMA_TrendUP = close > CPMA1
CPMA_TrendDwn = close < CPMA1

bullishCandle = candlepatternbullish() and CPMA_TrendUP[1] 
bearishCandle =  candlepatternbearish() and CPMA_TrendDwn[1]


Signal = 0
Signal := bullishCandle  ? 1 : bearishCandle  ? -1 : nz(Signal[1])
isDifferentSignalType = ta.change(Signal)

//plot on chart
plotshape(showCandles and bullishCandle and isDifferentSignalType , title="Bullish Candle", location=location.belowbar, style=shape.labelup, size=size.tiny, color=color.new(color.green, 0), textcolor=color.new(color.white, 0),text = "AI Bullish")
plotshape(showCandles and bearishCandle and isDifferentSignalType , title="Bearish Candle", location=location.abovebar, style=shape.labeldown, size=size.tiny, color=color.new(color.red, 0), textcolor=color.new(color.white, 0), text="AI Bearish")


// Get Bank Nifty data
//----------------HDFC
[w_r_t_Result1, ts1, ts1Chng, ts1p, VWAPColor, VWAPText, trend, BuySell, ADXColor, ADXText, RSIColor, RSIText, MFIColor, MFIText, AllG_Color, AllG_Text, MACD_Color, MACDText, FARMA_Color, FARMAText, ST_Color_21, ST_Text_21, ST_Color_14, ST_Text_14, ST_Color_10, ST_Text_10, RS_Text, RS_Color,CPMA]   = get_BankComponent_Details(t1)

//----------------ICIC
[w_r_t_Result2, ts2, ts2Chng, ts2p, VWAPColor2, VWAPText2,trend2, BuySell2, ADXColor2, ADXText2, RSIColor2, RSIText2, MFIColor2, MFIText2, AllG_Color2, AllG_Text2, MACD_Color2, MACDText2, FARMA_Color2, FARMAText2, ST_Color_212, ST_Text_212, ST_Color_142, ST_Text_142, ST_Color_102, ST_Text_102, RS_Text2, RS_Color2,CPMA2] = get_BankComponent_Details(t2)

//----------------KOTAK
[w_r_t_Result3, ts3, ts3Chng, ts3p, VWAPColor3, VWAPText3, trend3, BuySell3, ADXColor3, ADXText3, RSIColor3, RSIText3, MFIColor3, MFIText3, AllG_Color3, AllG_Text3, MACD_Color3, MACDText3, FARMA_Color3, FARMAText3, ST_Color_213, ST_Text_213, ST_Color_143, ST_Text_143, ST_Color_103, ST_Text_103, RS_Text3, RS_Color3, CPMA3] = get_BankComponent_Details(t3)

//----------------SBIN
[w_r_t_Result4, ts4, ts4Chng, ts4p, VWAPColor4, VWAPText4, trend4, BuySell4, ADXColor4, ADXText4, RSIColor4, RSIText4, MFIColor4, MFIText4, AllG_Color4, AllG_Text4, MACD_Color4, MACDText4, FARMA_Color4, FARMAText4, ST_Color_214, ST_Text_214, ST_Color_144, ST_Text_144, ST_Color_104, ST_Text_104, RS_Text4, RS_Color4, CPMA4] = get_BankComponent_Details(t4)

//----------------AXIS
[w_r_t_Result5, ts5, ts5Chng, ts5p, VWAPColor5, VWAPText5, trend5, BuySell5, ADXColor5, ADXText5, RSIColor5, RSIText5, MFIColor5, MFIText5, AllG_Color5, AllG_Text5, MACD_Color5, MACDText5, FARMA_Color5, FARMAText5,ST_Color_215, ST_Text_215, ST_Color_145, ST_Text_145, ST_Color_105, ST_Text_105, RS_Text5, RS_Color5, CPMA5] = get_BankComponent_Details(t5)

//----------------BANKNIFTY
[w_r_t_Result6, ts6, ts6Chng, ts6p, VWAPColor6, VWAPText6, trend6, BuySell6, ADXColor6, ADXText6, RSIColor6, RSIText6, MFIColor6, MFIText6, AllG_Color6, AllG_Text6, MACD_Color6, MACDText6, FARMA_Color6, FARMAText6, ST_Color_216, ST_Text_216, ST_Color_146, ST_Text_146, ST_Color_106, ST_Text_106, RS_Text6, RS_Color6, CPMA6]  = get_BankComponent_Details(t6)

//----------------HDFC
[w_r_t_Result7, ts7, ts7Chng, ts7p, VWAPColor7, VWAPText7, trend7, BuySell7, ADXColor7, ADXText7, RSIColor7, RSIText7, MFIColor7, MFIText7, AllG_Color7, AllG_Text7, MACD_Color7, MACDText7, FARMA_Color7, FARMAText7,ST_Color_217, ST_Text_217, ST_Color_147, ST_Text_147, ST_Color_107, ST_Text_107, RS_Text7, RS_Color7, CPMA7] = get_BankComponent_Details(t7)

//----------------RELIANCE
[w_r_t_Result8, ts8, ts8Chng, ts8p, VWAPColor8, VWAPText8, trend8, BuySell8, ADXColor8, ADXText8, RSIColor8, RSIText8, MFIColor8, MFIText8, AllG_Color8, AllG_Text8, MACD_Color8, MACDText8, FARMA_Color8, FARMAText8, ST_Color_218, ST_Text_218, ST_Color_148, ST_Text_148, ST_Color_108, ST_Text_108, RS_Text8, RS_Color8, CPMA8] = get_BankComponent_Details(t8)

//----------------TCS
[w_r_t_Result9, ts9, ts9Chng, ts9p, VWAPColor9, VWAPText9, trend9, BuySell9, ADXColor9, ADXText9, RSIColor9, RSIText9, MFIColor9, MFIText9, AllG_Color9, AllG_Text9, MACD_Color9, MACDText9, FARMA_Color9, FARMAText9, ST_Color_219, ST_Text_219, ST_Color_149, ST_Text_149, ST_Color_109, ST_Text_109, RS_Text9, RS_Color9, CPMA9] = get_BankComponent_Details(t9)

//----------------INFY
[w_r_t_Result10, ts10, ts10Chng, ts10p, VWAPColor10, VWAPText10, trend10, BuySell10, ADXColor10, ADXText10, RSIColor10, RSIText10, MFIColor10, MFIText10, AllG_Color10, AllG_Text10, MACD_Color10, MACDText10, FARMA_Color10, FARMAText10, ST_Color_2110, ST_Text_2110, ST_Color_1410, ST_Text_1410, ST_Color_1010, ST_Text_1010, RS_Text10, RS_Color10, CPMA10] = get_BankComponent_Details(t10)


//----------------Fedral
[w_r_t_Result11, ts11, ts11Chng, ts11p, VWAPColor11, VWAPText11, trend11, BuySell11, ADXColor11, ADXText11, RSIColor11, RSIText11, MFIColor11, MFIText11, AllG_Color11, AllG_Text11, MACD_Color11, MACDText11, FARMA_Color11, FARMAText11, ST_Color_2111, ST_Text_2111, ST_Color_1411, ST_Text_1411, ST_Color_1011, ST_Text_1011, RS_Text11, RS_Color11, CPMA11] = get_BankComponent_Details(t11)

CPMAcolor = close > CPMA1 ? color.green : close < CPMA1 ? color.red : color.white

plot(CPMA1, color=CPMAcolor, linewidth=2, title="CPMA")

funcUpdateTableCells( forRow,w_r_t_Result, scrpName, ts1, ts1Chng, ts1p, VWAPColor, VWAPText, trend, BuySell, ADXColor, ADXText, RSIColor, RSIText, MFIColor, MFIText, AllG_Color, AllG_Text, MACD_Color, MACDText, FARMA_Color, FARMAText, ST_Color_21, ST_Text_21, ST_Color_14, ST_Text_14, ST_Color_10, ST_Text_10, RS_Text, RS_Color)=>
    table.cell(t,1,forRow,str.tostring(w_r_t_Result),text_color=txt_col,text_size=table_text_size, bgcolor=color.new( w_r_t_Result == 'Buy' ? cell_up : w_r_t_Result == 'Sell' ? cell_dn : cell_netural,cell_transp))
    table.cell(t,2,forRow, str.replace_all(scrpName, 'NSE:', ''),text_color=#4caf50,text_size=table_text_size,bgcolor=color.black)
    table.cell(t,3,forRow, str.tostring(ts1) ,text_color=color.new(ts1p >= 0 ? cell_up : cell_dn ,cell_transp),text_size=table_text_size, bgcolor=color.black)
    table.cell(t,4,forRow, str.tostring(ts1Chng, '#.##'),text_color=color.new(ts1Chng >= 0 ? cell_up : cell_dn ,cell_transp),text_size=table_text_size, bgcolor=color.black)
    table.cell(t,5,forRow, str.tostring(ts1p),text_color=txt_col,text_size=table_text_size, bgcolor=color.new(ts1Chng >= 0 ? cell_up : cell_dn ,cell_transp) )
    table.cell(t,6,forRow,str.tostring(AllG_Text),text_color=txt_col,text_size=table_text_size, bgcolor= AllG_Color)
    table.cell(t,7,forRow,str.tostring(VWAPText),text_color=txt_col,text_size=table_text_size, bgcolor= VWAPColor)
    table.cell(t,8,forRow,str.tostring(BuySell),text_color=txt_col,text_size=table_text_size, bgcolor=color.new( trend ? cell_up : cell_dn ,cell_transp))
    table.cell(t,9,forRow,str.tostring(ADXText),text_color=txt_col,text_size=table_text_size, bgcolor= ADXColor)
    table.cell(t,10,forRow,str.tostring(RSIText),text_color=txt_col,text_size=table_text_size, bgcolor= RSIColor)
    table.cell(t,11,forRow,str.tostring(MFIText),text_color=txt_col,text_size=table_text_size, bgcolor= MFIColor)
    table.cell(t,12,forRow,str.tostring(MACDText),text_color=txt_col,text_size=table_text_size, bgcolor= MACD_Color)
    table.cell(t,13,forRow,str.tostring(FARMAText),text_color=txt_col,text_size=table_text_size, bgcolor= FARMA_Color)
    table.cell(t,14,forRow,str.tostring(ST_Text_21),text_color=txt_col,text_size=table_text_size, bgcolor= ST_Color_21)
    table.cell(t,15,forRow,str.tostring(ST_Text_14),text_color=txt_col,text_size=table_text_size, bgcolor= ST_Color_14)
    table.cell(t,16,forRow,str.tostring(ST_Text_10),text_color=txt_col,text_size=table_text_size, bgcolor= ST_Color_10)
   // table.cell(t,17,forRow,str.tostring(RS_Text),text_color=txt_col,text_size=table_text_size, bgcolor= RS_Color)

    Update = true

maxStockCount =  enableForSpecific ? 1 : 11
//========== Table Logic Start
if (barstate.islast and not enableTblHide) 
    table.cell(t,1,0,'RWR:'+timeframe.period,text_color=txt_col,text_size=table_text_size) 
    table.cell(t,2,0,'Symbol',text_color=txt_col,text_size=table_text_size)  
    table.cell(t,3,0,'LTP',text_color=txt_col,text_size=table_text_size)
    table.cell(t,4,0,'Chng',text_color=txt_col,text_size=table_text_size)
    table.cell(t,5,0,'Vol',text_color=txt_col,text_size=table_text_size)
    table.cell(t,6,0,'Alligator',text_color=txt_col,text_size=table_text_size)
    table.cell(t,7,0,'VWAP',text_color=txt_col,text_size=table_text_size)
    table.cell(t,8,0,'CPMA',text_color=txt_col,text_size=table_text_size)
    table.cell(t,9,0,'ADX',text_color=txt_col,text_size=table_text_size)
    table.cell(t,10,0,'RSI 30/70',text_color=txt_col,text_size=table_text_size)
    table.cell(t,11,0,'MFI 20/80',text_color=txt_col,text_size=table_text_size)
    table.cell(t,12,0,'MACD',text_color=txt_col,text_size=table_text_size)
    table.cell(t,13,0,'FARMA',text_color=txt_col,text_size=table_text_size)
    table.cell(t,14,0,'ST 21-1',text_color=txt_col,text_size=table_text_size)
    table.cell(t,15,0,'ST 14-2',text_color=txt_col,text_size=table_text_size)
    table.cell(t,16,0,'ST 10-3',text_color=txt_col,text_size=table_text_size)
   // table.cell(t,17,0,'RS',text_color=txt_col,text_size=table_text_size)

    for i = 1 to maxStockCount by 1
        if maxStockCount == 1 and enableForSpecific
            funcUpdateTableCells(i,w_r_t_Result11, t11,ts11, ts11Chng, ts11p, VWAPColor11, VWAPText11, trend11, BuySell11, ADXColor11, ADXText11, RSIColor11, RSIText11, MFIColor11, MFIText11, AllG_Color11, AllG_Text11, MACD_Color11, MACDText11, FARMA_Color11, FARMAText11, ST_Color_2111, ST_Text_2111, ST_Color_1411, ST_Text_1411, ST_Color_1011, ST_Text_1011, RS_Text11, RS_Color11)
        else
            switch
                i == 1 => funcUpdateTableCells(i,w_r_t_Result1, t1, ts1, ts1Chng, ts1p, VWAPColor, VWAPText, trend, BuySell, ADXColor, ADXText, RSIColor, RSIText, MFIColor, MFIText, AllG_Color, AllG_Text, MACD_Color, MACDText, FARMA_Color, FARMAText, ST_Color_21, ST_Text_21, ST_Color_14, ST_Text_14, ST_Color_10, ST_Text_10, RS_Text, RS_Color)
                i == 2 => funcUpdateTableCells(i,w_r_t_Result2, t2, ts2, ts2Chng, ts2p, VWAPColor2, VWAPText2,trend2, BuySell2, ADXColor2, ADXText2, RSIColor2, RSIText2, MFIColor2, MFIText2, AllG_Color2, AllG_Text2, MACD_Color2, MACDText2, FARMA_Color2, FARMAText2, ST_Color_212, ST_Text_212, ST_Color_142, ST_Text_142, ST_Color_102, ST_Text_102, RS_Text2, RS_Color2)
                i == 3 => funcUpdateTableCells(i,w_r_t_Result3, t3, ts3, ts3Chng, ts3p, VWAPColor3, VWAPText3, trend3, BuySell3, ADXColor3, ADXText3, RSIColor3, RSIText3, MFIColor3, MFIText3, AllG_Color3, AllG_Text3, MACD_Color3, MACDText3, FARMA_Color3, FARMAText3, ST_Color_213, ST_Text_213, ST_Color_143, ST_Text_143, ST_Color_103, ST_Text_103, RS_Text3, RS_Color3)
                i == 4 => funcUpdateTableCells(i,w_r_t_Result4, t4, ts4, ts4Chng, ts4p, VWAPColor4, VWAPText4, trend4, BuySell4, ADXColor4, ADXText4, RSIColor4, RSIText4, MFIColor4, MFIText4, AllG_Color4, AllG_Text4, MACD_Color4, MACDText4, FARMA_Color4, FARMAText4, ST_Color_214, ST_Text_214, ST_Color_144, ST_Text_144, ST_Color_104, ST_Text_104, RS_Text4, RS_Color4)
                i == 5 => funcUpdateTableCells(i,w_r_t_Result5, t5, ts5, ts5Chng, ts5p, VWAPColor5, VWAPText5, trend5, BuySell5, ADXColor5, ADXText5, RSIColor5, RSIText5, MFIColor5, MFIText5, AllG_Color5, AllG_Text5, MACD_Color5, MACDText5, FARMA_Color5, FARMAText5,ST_Color_215, ST_Text_215, ST_Color_145, ST_Text_145, ST_Color_105, ST_Text_105, RS_Text5, RS_Color5)
                i == 6 => funcUpdateTableCells(i,w_r_t_Result6, t6, ts6, ts6Chng, ts6p, VWAPColor6, VWAPText6, trend6, BuySell6, ADXColor6, ADXText6, RSIColor6, RSIText6, MFIColor6, MFIText6, AllG_Color6, AllG_Text6, MACD_Color6, MACDText6, FARMA_Color6, FARMAText6, ST_Color_216, ST_Text_216, ST_Color_146, ST_Text_146, ST_Color_106, ST_Text_106, RS_Text6, RS_Color6)
                i == 7 => funcUpdateTableCells(i,w_r_t_Result7, t7, ts7, ts7Chng, ts7p, VWAPColor7, VWAPText7, trend7, BuySell7, ADXColor7, ADXText7, RSIColor7, RSIText7, MFIColor7, MFIText7, AllG_Color7, AllG_Text7, MACD_Color7, MACDText7, FARMA_Color7, FARMAText7,ST_Color_217, ST_Text_217, ST_Color_147, ST_Text_147, ST_Color_107, ST_Text_107, RS_Text7, RS_Color7)
                i == 8 => funcUpdateTableCells(i,w_r_t_Result8, t8, ts8, ts8Chng, ts8p, VWAPColor8, VWAPText8, trend8, BuySell8, ADXColor8, ADXText8, RSIColor8, RSIText8, MFIColor8, MFIText8, AllG_Color8, AllG_Text8, MACD_Color8, MACDText8, FARMA_Color8, FARMAText8, ST_Color_218, ST_Text_218, ST_Color_148, ST_Text_148, ST_Color_108, ST_Text_108, RS_Text8, RS_Color8)
                i == 9 => funcUpdateTableCells(i,w_r_t_Result9, t9,ts9, ts9Chng, ts9p, VWAPColor9, VWAPText9, trend9, BuySell9, ADXColor9, ADXText9, RSIColor9, RSIText9, MFIColor9, MFIText9, AllG_Color9, AllG_Text9, MACD_Color9, MACDText9, FARMA_Color9, FARMAText9, ST_Color_219, ST_Text_219, ST_Color_149, ST_Text_149, ST_Color_109, ST_Text_109, RS_Text9, RS_Color9)
                i == 10 => funcUpdateTableCells(i,w_r_t_Result10,t10 ,ts10, ts10Chng, ts10p, VWAPColor10, VWAPText10, trend10, BuySell10, ADXColor10, ADXText10, RSIColor10, RSIText10, MFIColor10, MFIText10, AllG_Color10, AllG_Text10, MACD_Color10, MACDText10, FARMA_Color10, FARMAText10, ST_Color_2110, ST_Text_2110, ST_Color_1410, ST_Text_1410, ST_Color_1010, ST_Text_1010, RS_Text10, RS_Color10)
                i == 11 => funcUpdateTableCells(i,w_r_t_Result11,t11,ts11, ts11Chng, ts11p, VWAPColor11, VWAPText11, trend11, BuySell11, ADXColor11, ADXText11, RSIColor11, RSIText11, MFIColor11, MFIText11, AllG_Color11, AllG_Text11, MACD_Color11, MACDText11, FARMA_Color11, FARMAText11, ST_Color_2111, ST_Text_2111, ST_Color_1411, ST_Text_1411, ST_Color_1011, ST_Text_1011, RS_Text11, RS_Color11)