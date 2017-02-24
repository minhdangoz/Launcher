package com.klauncher.kinflow.cards;

import android.content.Context;

import com.klauncher.launcher.BuildConfig;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.utilities.FileUtils;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.utilities.NetworkUtils;
import com.klauncher.kinflow.utilities.ProtocolConst;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanni on 15/12/4.
 */
public class CardsListManager {

    private static final String DEF_CARD_LIST_PATH = "default_card_list";
    private static final String CARD_LIST_PATH = "card_list";

    private static final String CARD_LIST_FIELD_UPDATE_INTERVAL = "updateInterval";
    private static final String CARD_LIST_FIELD_LAST_MODIFIED = "lastModified";
    private static final String CARD_LIST_FIELD_CARDS = "cards";

    private static final long DEFAULT_CARD_REQUEST_INTERVAL = 24 * 3600 * 1000;

    private Context mContext;
    private CardsList mCurCardsList;

    private class CardsList {

        final long updateInterval;
        final long lastModified;
        final List<CardInfo> cards = new ArrayList<>();

        private CardsList(long updateInterval, long lastModified) {
            if (updateInterval > 0) {
                this.updateInterval = updateInterval;
            } else {
                this.updateInterval = DEFAULT_CARD_REQUEST_INTERVAL;
            }
            this.lastModified = lastModified;
        }

        void addSite(JSONObject siteJsonNode) throws JSONException {
            CardInfo info = new CardInfo(siteJsonNode, mContext);
            cards.add(info);
        }

        List<CardInfo> getCardsList() {
            return cards;
        }

        String toJSONString() {
            JSONObject rootJson = new JSONObject();
            try {
                rootJson.put(CARD_LIST_FIELD_LAST_MODIFIED, lastModified);
                rootJson.put(CARD_LIST_FIELD_UPDATE_INTERVAL, updateInterval);
                JSONArray sites = new JSONArray();
                for (CardInfo card : cards) {
                    JSONObject siteJson = new JSONObject();
                    siteJson.put(CardInfo.CARD_ID, card.getCardId());
                    siteJson.put(CardInfo.CARD_NAME, card.getCardName());
                    siteJson.put(CardInfo.CARD_ORDER, card.getCardOrder());
                    siteJson.put(CardInfo.CARD_TYPE_ID, card.getCardTypeId() + "," + card.getCardSecondTypeId());
                    siteJson.put(CardInfo.CARD_FOOTER, card.getCardFooter());
                    List<String> optionsList = card.getCardOpenOptionList();
                    JSONArray opts = new JSONArray();
                    for (String option : optionsList) {
                        opts.put(option);
                    }
                    siteJson.put(CardInfo.CARD_EXTRA, opts);
                    sites.put(siteJson);
                }
                rootJson.put(CARD_LIST_FIELD_CARDS, sites);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return rootJson.toString();
        }
    }

    private static CardsListManager sInstance = new CardsListManager();
    public static CardsListManager getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mContext = context;
    }

    public List<CardInfo> getInfos() {
        loadCardList();
        return mCurCardsList.getCardsList();
    }

    /**
     * 获取cardsList这个对象
     * 本类,初始化的时候调用
     * 刷新的时候也要调用loadCardList()
     */
    public void loadCardList() {
        log("try load from files");
        CardsList cardsList = loadDownloadCards();
        if (cardsList == null) {
            cardsList = loadDefaultCards();
        }
        if (cardsList == null) {
            return;
        }
        mCurCardsList = cardsList;
        tryRequest();
    }

    private void tryRequest() {
        log("tryRequest");
        long nextUpdateTime = mCurCardsList.lastModified + mCurCardsList.updateInterval;
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > nextUpdateTime) {
            requestNewCardList();
        }
    }

    public void requestNewCardList() {
        log("requestNewCardList");
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpGetNewCardList();
            }
        }).start();
    }

    private void httpGetNewCardList() {
        String urlString = BuildConfig.PUSH_DOMAIN + ProtocolConst.CARD_UPDATE_URL;
        HttpURLConnection conn = null;
        InputStream is = null;
        String resultData = "";

        try {
            URL url = new URL(urlString);
            Proxy proxy = NetworkUtils.getProxy(new URI(urlString));
            if (proxy != null) {
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setConnectTimeout(ProtocolConst.HTTP_CONNECT_TIMEOUT);
            conn.setReadTimeout(ProtocolConst.HTTP_READ_TIMEOUT);
            if (conn.getResponseCode() == 200 &&
                    ProtocolConst.CONTENT_TYPE_JSON.equalsIgnoreCase(conn.getContentType())) {
                is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(isr);
                String inputLine;
                while ((inputLine = bufferReader.readLine()) != null) {
                    resultData += inputLine + "\n";
                }
                if (!resultData.isEmpty()) {
                    log("httpGetNewCardList success");
                    FileUtils.write(resultData, getNavListFile(),
                            Charset.defaultCharset());
                }
            } else if (conn.getResponseCode() == 304) {
                log("httpGetNewCardList not modify");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private CardsList loadDefaultCards() {
        log("loadDefaultCards");
        try {
            InputStream is = mContext.getAssets().open(DEF_CARD_LIST_PATH);
            String json = FileUtils.loadStringFromStream(is);
            return loadCards(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getNavListFile() {
        return new File(mContext.getFilesDir().getAbsolutePath() + "/" + CARD_LIST_PATH);
    }

    private CardsList loadDownloadCards() {
        CardsList cardsList = null;
        try {
            File navFile = getNavListFile();
            if (navFile.exists()) {
                String json = FileUtils.loadStringFromStream(new FileInputStream(navFile));
                cardsList = loadCards(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cardsList;
    }

    private CardsList loadCards(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        long updateInterval = jsonObject.getLong(CARD_LIST_FIELD_UPDATE_INTERVAL);
        long lastModified = jsonObject.getLong(CARD_LIST_FIELD_LAST_MODIFIED);
        CardsList cardsList = new CardsList(updateInterval, lastModified);
        JSONArray sitesJsonArray = jsonObject.getJSONArray(CARD_LIST_FIELD_CARDS);
        if (sitesJsonArray != null && sitesJsonArray.length() > 0) {
            for(int i = 0; i<sitesJsonArray.length(); i++) {
                cardsList.addSite(sitesJsonArray.getJSONObject(i));
            }
        }
        return cardsList;
    }

    public void saveNavs() {
        FileUtils.write(mCurCardsList.toJSONString(), getNavListFile(), Charset.defaultCharset());
    }

    final protected static void log(String msg) {
        KinflowLog.d(msg);
    }
}
