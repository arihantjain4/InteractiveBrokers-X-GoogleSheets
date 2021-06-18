import com.ib.client.*;

import java.io.IOException;
import java.util.*;
import java.io.*;

public class Main implements EWrapper {
    public Map<Integer, Character> alphabet = new HashMap<>();
    private static EClientSocket clientSocket;
    private static EJavaSignal readerSignal;
    private static EReader eReader;
    public static ArrayList<String> tickSymbol = new ArrayList<>();
    public static ArrayList<String> actionString = new ArrayList<>();
    public static ArrayList<Double> lmtPriceDouble = new ArrayList<>();
    public static ArrayList<Integer> orderQuantity = new ArrayList<>();
    public static ArrayList<Boolean> buyNew = new ArrayList<>();
    private int orderidE = 1;
    public Main(){
        String alph = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (char c : alph.toCharArray()){
            alphabet.put(alph.indexOf(c), c);
        }
    }
    public void mainfunc() throws InterruptedException, IOException {
        //System.out.println(tickSymbol.toString()+actionString.toString()+lmtPriceDouble.toString()+orderQuantity);
        readerSignal = new EJavaSignal();
        clientSocket = new EClientSocket(this, readerSignal);
        clientSocket.eConnect("127.0.0.1", 7497, 0);
        eReader = new EReader(clientSocket, readerSignal);
        System.out.println(clientSocket.isConnected());
        eReader.start();
        Thread processer = new Thread(() -> {
            while ( clientSocket.isConnected() ) {
                readerSignal.waitForSignal();
                try {
                    eReader.processMsgs();
                } catch (IOException ex) {}
            }
        });
        processer.setDaemon(true);
        processer.start();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<tickSymbol.size();i++){
                    int finalI = i;
                    if (buyNew.get(finalI) && !actionString.get(finalI).equals("HOLD")) {
                        try {
                            clientSocket.placeOrder(orderidE, contractFill("CUSIP", "USD", tickSymbol.get(finalI), "STK", "SMART"), orderFill(lmtPriceDouble.get(finalI), actionString.get(finalI), "LMT", "GTC", orderQuantity.get(finalI), "DU3733033", 0));
                            Interactive.setOrderStatus(tickSymbol.get(finalI), "PLACED", 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }//symbol, action, limit price, and quant
                        orderidE++;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        };

        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
        //System.out.println("after thread?");

        /*Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            clientSocket.reqExecutions(10001, new ExecutionFilter());
            clientSocket.reqCompletedOrders(false);
            }
        },5000,10000);*/
    }

    public static Order orderFill(double lmtPrice, String action, String orderType, String tif, int totalQuantity, String account, int clientId){
        Order order = new Order();
        /*order.m_lmtPrice = lmtPrice;
        order.m_action = action;
        order.m_orderType = orderType;
        order.m_tif = tif;
        order.m_totalQuantity = totalQuantity;
        order.m_account = account;
        order.m_clientId = clientId;*/
        order.lmtPrice(lmtPrice);
        order.action(action);
        order.orderType(orderType);
        order.tif(tif);
        order.totalQuantity(totalQuantity);
        order.account(account);
        order.clientId(clientId);

        return order;
    }
    public static Contract contractFill(String secIdType, String currency, String symbol, String secType, String exchange){
        Contract contract = new Contract();
        /*contract.m_secIdType = secIdType;
        contract.m_currency = currency;
        contract.m_symbol = symbol;
        contract.m_secType = secType;
        contract.m_exchange = exchange;*/
        contract.secIdType(secIdType);
        contract.currency(currency);
        contract.symbol(symbol);
        contract.secType(secType);
        contract.exchange(exchange);
        return contract;
    }
    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {

    }

    @Override
    public void tickSize(int tickerId, int field, int size) {

    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {

    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {

    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {

    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {

    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {

    }

    @Override
    public void openOrderEnd() {

    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {

    }

    @Override
    public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {

    }

    @Override
    public void updateAccountTime(String timeStamp) {

    }

    @Override
    public void accountDownloadEnd(String accountName) {

    }

    @Override
    public void nextValidId(int orderId) {
        orderidE = orderId;
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {

    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {

    }

    @Override
    public void contractDetailsEnd(int reqId) {

    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) throws IOException {
        System.out.println("FROM EXECDETAILS\n" + "SIDE: " + execution.side() + "\n");
        if (execution.side().equals("BOT")){
            Interactive.setOrderStatus(contract.symbol(), "EXECUTED", (int) execution.shares());
        }
        else if (execution.side().equals("SLD")){
            Interactive.setOrderStatus(contract.symbol(), "EXECUTED", (int) (-1 *execution.shares()));
        }
    }

    @Override
    public void execDetailsEnd(int reqId) {
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {

    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size, boolean isSmartDepth) {

    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {

    }

    @Override
    public void managedAccounts(String accountsList) {

    }

    @Override
    public void receiveFA(int faDataType, String xml) {

    }

    @Override
    public void historicalData(int reqId, Bar bar) {

    }

    @Override
    public void scannerParameters(String xml) {

    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {

    }

    @Override
    public void scannerDataEnd(int reqId) {

    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {

    }

    @Override
    public void currentTime(long time) {

    }

    @Override
    public void fundamentalData(int reqId, String data) {

    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {

    }

    @Override
    public void tickSnapshotEnd(int reqId) {

    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {

    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {

    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {

    }

    @Override
    public void positionEnd() {

    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {

    }

    @Override
    public void accountSummaryEnd(int reqId) {

    }

    @Override
    public void verifyMessageAPI(String apiData) {

    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {

    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {

    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {

    }

    @Override
    public void displayGroupList(int reqId, String groups) {

    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {

    }

    @Override
    public void error(Exception e) {

    }

    @Override
    public void error(String str) {

    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectAck() {

    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {

    }

    @Override
    public void positionMultiEnd(int reqId) {

    }

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {

    }

    @Override
    public void accountUpdateMultiEnd(int reqId) {

    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {

    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {

    }

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {

    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {

    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {

    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {

    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {

    }

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {

    }

    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {

    }

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {

    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {

    }

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {

    }

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {

    }

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {

    }

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {

    }

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {

    }

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {

    }

    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {

    }

    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {

    }

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {

    }

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {

    }

    @Override
    public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {

    }

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {

    }

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {

    }

    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {

    }

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast, String exchange, String specialConditions) {

    }

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttribBidAsk tickAttribBidAsk) {

    }

    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {

    }

    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {

    }

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {

    }
    @Override
    public void completedOrdersEnd() {
    }
}
