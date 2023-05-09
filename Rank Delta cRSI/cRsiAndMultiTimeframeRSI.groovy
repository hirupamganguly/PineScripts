//@version=5
indicator("Wilder / Connors Relative Strength Index Candle with Heikin Ashi", "CRSICHA", false, format.price, 2, explicit_plot_zorder = true)

getloc(bar_i, prd) => //{
    _ret = bar_index + prd - bar_i
    _ret
getloval(l1, l2, b1val, b2val, b3val) => //{
    _ret1 = l1 == 1 ? b1val : l1 == 2 ? b2val : l1 == 3 ? b3val : 0
    _ret2 = l2 == 1 ? b1val : l2 == 2 ? b2val : l2 == 3 ? b3val : 0
    [_ret1, _ret2]

gtlopos(l1, l2, b1pos, b2pos, b3pos) => //{
    _ret1 = l1 == 1 ? b1pos : l1 == 2 ? b2pos : l1 == 3 ? b3pos : 0
    _ret2 = l2 == 1 ? b1pos : l2 == 2 ? b2pos : l2 == 3 ? b3pos : 0
    [_ret1, _ret2]
gethival(l1, l2, t1val, t2val, t3val) => //{
    _ret1 = l1 == 1 ? t1val : l1 == 2 ? t2val : l1 == 3 ? t3val : 0
    _ret2 = l2 == 1 ? t1val : l2 == 2 ? t2val : l2 == 3 ? t3val : 0
    [_ret1, _ret2]
gethipos(l1, l2, t1pos, t2pos, t3pos) => //{
    _ret1 = l1 == 1 ? t1pos : l1 == 2 ? t2pos : l1 == 3 ? t3pos : 0
    _ret2 = l2 == 1 ? t1pos : l2 == 2 ? t2pos : l2 == 3 ? t3pos : 0
    [_ret1, _ret2]
_inRange(cond, rangeLower, rangeUpper) => // {
    bars = ta.barssince(cond == true)
    rangeLower <= bars and bars <= rangeUpper
ema(src, len) => //{
    alpha = 2 / (len + 1)
    sum   = 0.0
    sum  := na(sum[1]) ? src : alpha * src + (1 - alpha) * nz(sum[1])
tma(src, int len) => //{
    ema1 = ema(src, len)
    ema2 = ema(ema1, len)
    ema3 = ema(ema2, len)
    tma  = 3 * (ema1 - ema2) + ema3



enable     = input.bool(false, "Enable Heikin Ashi", group="RSI Settings", inline = "can")
red        = input.color(color.new(#ef5350, 0), "", group="RSI Settings", inline = "can")
green      = input.color(color.new(#26a69a, 0), "", group="RSI Settings", inline = "can")
select     = input.string("RSI", "Select RSI Type", ["RSI","CRSI"], group="RSI Settings")
length     = input.int(14, "RSI Length", 1, 5000, 1, group="RSI Settings")
c_length   = input.int(3, "CRSI Length", 1, 5000, 1, group="RSI Settings")
ud_length  = input.int(2, "CRSI Up Down Length", 1, 5000, 1, group="RSI Settings")
roc_length = input.int(100,"CRSI ROC Length", 1, 5000, 1, group="RSI Settings")

ma_enable     = input.bool(false, "MA Enable", group="MA Settings")
maTypeInput   = input.string("SMA", title="MA Type", options=["SMA", "Bollinger Bands", "EMA", "TMA", "SMMA (RMA)", "WMA", "VWMA"], group="MA Settings")
maLengthInput = input.int(14, title="MA Length", group="MA Settings")
bbMultInput   = input.float(2.0, minval=0.001, maxval=50, title="BB StdDev", group="MA Settings")

prd                   = input.int(10, 'Pivot Point Period', 5, 50, group = "CBI")
PPnum                 = input.int(3, 'Number of Pivot Point to check', 2, 3, group = "CBI")
enable_regular        = input.bool(true, "Enable Regular Divergance", group = "Divergence")
enable_hidden         = input.bool(false, "Enable Hidden Divergance", group = "Divergence")
ext_s                 = input.string("Price", "Divergence Source", ["Price","RSI"], group = "Divergence")
ext_sw                = ext_s == "RSI" ? true : ext_s == "Price" ? false : false
div_rsi               = input.int(14,"Divergence RSI Length", group = "Divergence")
lbR                   = input.int(5, 'Pivot Lookback Right', tooltip = "Old default: 3. Fiddle with this if its not picking up divergence.", group = "Divergence")
lbL                   = input.int(5, 'Pivot Lookback Left', tooltip = "Old default: 6. Fiddle with this if its not picking up divergence." ,group = "Divergence")
rangeUpper            = input.int(60, 'Max of Lookback Range', group = "Divergence")
rangeLower            = input.int(10, 'Min of Lookback Range', group = "Divergence")
delay_plot_til_closed = input.bool(false, 'Delay plot until candle is closed (don\'t repaint)', group = "Divergence")
mark                  = input.bool(true, 'Enable Markers', group = 'Settings')

div(osc, ext = close) => //{
    offset  = -lbR
    repaint = not delay_plot_til_closed or barstate.ishistory or barstate.isconfirmed //}
    High    = not ext_sw ? high[lbR] : ext[lbR]
    Low     = not ext_sw ?  low[lbR] : ext[lbR]
    dHigh   = not ext_sw ? high[lbR] : ext[lbR]
    dLow    = not ext_sw ?  low[lbR] : ext[lbR] //}
    plFound = na(ta.pivotlow(osc, lbL, lbR))  ? false : true
    phFound = na(ta.pivothigh(osc, lbL, lbR)) ? false : true //}
    oscHL   = osc[lbR] > ta.valuewhen(plFound, osc[lbR], 1) and _inRange(plFound[1], rangeLower, rangeUpper)
    oscLH   = osc[lbR] < ta.valuewhen(phFound, osc[lbR], 1) and _inRange(phFound[1], rangeLower, rangeUpper)
    oscLL   = osc[lbR] < ta.valuewhen(plFound, osc[lbR], 1) and _inRange(plFound[1], rangeLower, rangeUpper)
    oscHH   = osc[lbR] > ta.valuewhen(phFound, osc[lbR], 1) and _inRange(phFound[1], rangeLower, rangeUpper) //}
    priceHL =  dLow[lbR] > ta.valuewhen(plFound, dLow[lbR], 1)
    priceLH = dHigh[lbR] < ta.valuewhen(phFound, dHigh[lbR], 1)
    priceLL =        Low < ta.valuewhen(plFound, Low, 1)
    priceHH =       High > ta.valuewhen(phFound, High, 1) //}
    bullCond       =       priceLL and oscHL and plFound and repaint 
    bearCond       =       priceHH and oscLH and phFound and repaint
    hiddenBullCond = enable_hidden and priceHL and oscLL and plFound and repaint
    hiddenBearCond = enable_hidden and priceLH and oscHH and phFound and repaint //}
    bear     = bearCond ? osc[lbR] : na
    bull     = bullCond ? osc[lbR] : na
    bullish  = plFound ? osc[lbR] : na
    bearish  = phFound ? osc[lbR] : na
    hbullish = plFound ? osc[lbR] : na
    hbearish = phFound ? osc[lbR] : na
    hbull    = hiddenBullCond ? osc[lbR] : na
    hbear    = hiddenBearCond ? osc[lbR] : na
    [bullish, bearish, bull, bear, hbullish, hbearish, hbull, hbear, offset]

ma(source, length, type) =>
    switch type
        "SMA" => ta.sma(source, length)
        "Bollinger Bands" => ta.sma(source, length)
        "EMA" => ema(source, length)
        "TMA" => tma(source, length)
        "SMMA (RMA)" => ta.rma(source, length)
        "WMA" => ta.wma(source, length)
        "VWMA" => ta.vwma(source, length)

rsi(float input = close, int length = 14) =>
    up   = ta.rma(math.max(ta.change(input), 0), length)
    down = ta.rma(-math.min(ta.change(input), 0), length)
    out  = down == 0 ? 100 : up == 0 ? 0 : 100 - (100 / (1 + up / down))

updown(float s) =>
	isEqual = s == s[1]
	isGrowing = s > s[1]
	ud = 0.0
	ud := isEqual ? 0 : isGrowing ? (nz(ud[1]) <= 0 ? 1 : nz(ud[1])+1) : (nz(ud[1]) >= 0 ? -1 : nz(ud[1])-1)
	ud

crsi(float input = close, simple int length = 3, simple int ud_length = 2, simple int roc_length = 100) =>
	updown      = ta.rsi(updown(input), length)
    rsi         = ta.rsi(input, length)
    updownrsi   = ta.rsi(updown, ud_length)
    percentrank = ta.percentrank(ta.roc(input, 1), roc_length)
    crsi        = math.avg(rsi, updownrsi, percentrank)
    crsi

rsi_select(series float input = close, simple int length = 14, simple int c_length = 3, simple string select = "RSI", simple int ud_length = 2, simple int roc_length = 100) =>
    switch select
        "RSI"  => rsi(input, length)
        "CRSI" => crsi(input, c_length, ud_length, roc_length)

ha_close(float Open = open, float High = high, float Low = low, float Close = close, bool enable = true) =>
    ha_close = (Open + High + Low + Close) / 4
    out = enable == true ? ha_close : Close
 
ha_open(float Open = open, float High = high, float Low = low, float Close = close, bool enable = true) =>
    ha_open  = float(na)
    ha_close = ha_close(Open, High, Low, Close)
    ha_open := na(ha_open[1]) ? (Open + Close) / 2 : (nz(ha_open[1]) + nz(ha_close[1])) / 2 
    out = enable == true ? ha_open : Open

ha_high(float Open = open, float High = high, float Low = low, float Close = close, bool enable = true) =>
    ha_close = ha_close(Open, High, Low, Close)
    ha_open  = ha_open(Open, High, Low, Close)
    ha_high  = math.max(High, math.max(ha_open, ha_close))
    out = enable == true ? ha_high : High

ha_low(float Open = open, float High = high, float Low = low, float Close = close, bool enable = true) =>
    ha_close = ha_close(Open, High, Low, Close)
    ha_open  = ha_open(Open, High, Low, Close)
    ha_low = math.min(Low,  math.min(ha_open, ha_close))
    out = enable == true ? ha_low : Low

rsi_open  = rsi_select(open, length, c_length, select, ud_length, roc_length)
rsi_high  = rsi_select(high, length, c_length, select, ud_length, roc_length)
rsi_low   = rsi_select(low, length, c_length, select, ud_length, roc_length)
rsi_close = rsi_select(close, length, c_length, select, ud_length, roc_length)

Open  = ha_open(rsi_open, rsi_high, rsi_low, rsi_close, enable)
High  = ha_high(rsi_open, rsi_high, rsi_low, rsi_close, enable)
Low   = ha_low(rsi_open, rsi_high, rsi_low, rsi_close, enable)
Close = ha_close(rsi_open, rsi_high, rsi_low, rsi_close, enable)

rsiMA = ma(Close, maLengthInput, maTypeInput)
isBB = maTypeInput == "Bollinger Bands"

colour   = Open < Close ? green : red
ma_alpha = ma_enable == true ? 0  : 100
bb_alpha = ma_enable == true ? 90 : 100

[bullish, bearish, bull, bear, hbullish, hbearish, hbull, hbear, offset] = div(Close)

rsiLengthInput = input.int(14, minval=1, title="RSI Length", group="RSI Settings")
rsiSourceInput = input.source(close, "Source", group="RSI Settings")
ob = input.float(70,step=10,title="Over Bought level",group='RSI Settings')
os   = input.float(30,step=10,title="Over Sold level",group='RSI Settings')

MTF1 = input.timeframe('15', "TimeFrame 1",group='TimeFrame Settings')
MTF2 = input.timeframe('60', "TimeFrame 2",group='TimeFrame Settings')
MTF3 = input.timeframe('120', "TimeFrame 3",group='TimeFrame Settings')
MTF4 = input.timeframe('D', "TimeFrame 4",group='TimeFrame Settings')
head_tcolor = input(color.new(#ffffff,70),'Header Text Color',group='Table Style')
head_area = input(color.new(#ccffff, 5),'Header Background Color',group='Table Style')
ob_area = input(color.new(#0cb51a,70),'Over Bought Background Color',group='Table Style')
os_area = input(color.new(#ff1100,70),'Over Sold Background Color',group='Table Style')
mid_area = input(color.new(#ff1100,70),'Mid-Range Background Color',group='Table Style')
ob_tcolor = input(color.new(#0cb51a,70),'Over Bought Text Color',group='Table Style')
os_tcolor = input(color.new(#ff1100,70),'Over Sold Text Color',group='Table Style')
mid_tcolor = input(color.new(#ff1100,70),'Mid-Range Text Color',group='Table Style')


RSIMFT1 = request.security(syminfo.tickerid, MTF1, ta.rsi(rsiSourceInput,rsiLengthInput))
RSIMFT2 = request.security(syminfo.tickerid, MTF2, ta.rsi(rsiSourceInput,rsiLengthInput))
RSIMFT3 = request.security(syminfo.tickerid, MTF3, ta.rsi(rsiSourceInput,rsiLengthInput))
RSIMFT4 = request.security(syminfo.tickerid, MTF4, ta.rsi(rsiSourceInput,rsiLengthInput))

RSIMFT1bg =
     (RSIMFT1 >= ob) ? ob_area :
     (RSIMFT1 <= os) ? os_area :
     (RSIMFT1 < ob and RSIMFT1 > os) ? mid_area :
     na

RSIMFT2bg =
     (RSIMFT2 >= ob) ? ob_area :
     (RSIMFT2 <= os) ? os_area :
     (RSIMFT2 < ob and RSIMFT2 > os) ? mid_area :
     na

RSIMFT3bg =
     (RSIMFT3 >= ob) ? ob_area :
     (RSIMFT3 <= os) ? os_area :
     (RSIMFT3 < ob and RSIMFT3 > os) ? mid_area :
     na

RSIMFT4bg =
     (RSIMFT4 >= ob) ? ob_area :
     (RSIMFT4 <= os) ? os_area :
     (RSIMFT4 < ob and RSIMFT4 > os) ? mid_area :
     na

RSIMFT1txt =
     (RSIMFT1 >= ob) ? ob_tcolor :
     (RSIMFT1 <= os) ? os_tcolor :
     (RSIMFT1 < ob and RSIMFT1 > os) ? mid_tcolor :
     na

RSIMFT2txt =
     (RSIMFT2 >= ob) ? ob_tcolor :
     (RSIMFT2 <= os) ? os_tcolor :
     (RSIMFT2 < ob and RSIMFT2 > os) ? mid_tcolor :
     na

RSIMFT3txt =
     (RSIMFT3 >= ob) ? ob_tcolor :
     (RSIMFT3 <= os) ? os_tcolor :
     (RSIMFT3 < ob and RSIMFT3 > os) ? mid_tcolor :
     na

RSIMFT4txt =
     (RSIMFT4 >= ob) ? ob_tcolor :
     (RSIMFT4 <= os) ? os_tcolor :
     (RSIMFT4 < ob and RSIMFT4 > os) ? mid_tcolor :
     na

//----
var tb = table.new(position.top_right,4,2)
if barstate.islast
    table.cell(tb,0,0,MTF1,text_color=head_tcolor,bgcolor=head_area)
    table.cell(tb,1,0,MTF2,text_color=head_tcolor,bgcolor=head_area)
    table.cell(tb,2,0,MTF3,text_color=head_tcolor,bgcolor=head_area)
    table.cell(tb,3,0,MTF4,text_color=head_tcolor,bgcolor=head_area)
    
    table.cell(tb,0,1,str.tostring(RSIMFT1,'#.##'),text_color=RSIMFT1txt,bgcolor=RSIMFT1bg)
    table.cell(tb,1,1,str.tostring(RSIMFT2,'#.##'),text_color=RSIMFT2txt,bgcolor=RSIMFT2bg)
    table.cell(tb,2,1,str.tostring(RSIMFT3,'#.##'),text_color=RSIMFT3txt,bgcolor=RSIMFT3bg)
    table.cell(tb,3,1,str.tostring(RSIMFT4,'#.##'),text_color=RSIMFT4txt,bgcolor=RSIMFT4bg)




//----

plot(rsiMA, "RSI-based MA", color= color.new(color.yellow, ma_alpha))
bbUpperBand = plot(isBB ? rsiMA + ta.stdev(Close, maLengthInput) * bbMultInput : na, title = "Upper Bollinger Band", color=color.new(color.green, ma_alpha))
bbLowerBand = plot(isBB ? rsiMA - ta.stdev(Close, maLengthInput) * bbMultInput : na, title = "Lower Bollinger Band", color=color.new(color.green, ma_alpha))
plotcandle(Open, High, Low, Close, "RSI Candle", colour, colour, true, bordercolor = colour)
fill(bbUpperBand, bbLowerBand, color= isBB ? color.new(color.green, bb_alpha) : na, title="Bollinger Bands Background Fill")
rsiUpperBand = hline(70, "RSI Upper Band", color=#787B86)
hline(50, "RSI Middle Band", color.new(#787B86, 50))
rsiLowerBand = hline(30, "RSI Lower Band", #787B86)
fill(rsiUpperBand, rsiLowerBand, color=color.rgb(126, 87, 194, 90), title="RSI Background Fill")

plot(enable_regular ? bullish : na, 'Bullish', bull ? color.new(color.green, 0) : color.new(color.white, 100), offset = offset)
plot(enable_regular ? bearish : na, 'Bearish', bear ? color.new(color.red, 0) : color.new(color.white, 100), offset = offset)
plot(hbullish, 'Hidden Bullish', hbull ? color.new(color.yellow, 0) : color.new(color.white, 100), offset = offset)
plot(hbearish, 'Hidden Bearish', hbear ? color.new(color.purple, 0) : color.new(color.white, 100), offset = offset)
plotshape(bull and mark, "Bullish Divergence", shape.triangleup, location.bottom, color.new(color.green, 0))
plotshape(bear and mark, "Bearish Divergance", shape.triangledown, location.top, color.new(color.red, 0))
plotshape(hbull and mark, "Hidden Bullish Divergence", shape.triangleup, location.bottom,  hbull ? color.new(color.yellow, 0) : color.new(color.white, 100))
plotshape(hbear and mark, "Hidden Bearish Divergance", shape.triangledown, location.top, hbear ? color.new(color.purple, 0) : color.new(color.white, 100))
