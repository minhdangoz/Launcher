package com.alpsdroid.ads;

/**
 * 应用广告素材
 * <br><br>
 * 支持应用广告的内容必须继承该接口，用于提供packagename、版本号控制
 */
public interface AppCreative {
    /**
     * 获取应用ID
     * <br><br>
     * 应用ID用于获取对应应用信息<br>
     *
     * @return 应用ID
     */
    public String getAppID();

    /**
     * 获取应用包名根据包名进行列表去重
     *
     * @return 应用包名
     */
    public String getPackageName();

    /**
     * 获取应用version code
     * <br><br>
     * 通过version code识别是否要更新本地内容。<br>
     * 版本号数值越高，代表内容越新。
     *
     * @return 内容的版本号
     */
    public int getVersionCode();

//    /**
//     * 获取内容的序列化文本信息
//     *
//     * @return 内容的序列化文本信息
//     */
//    public String getContent();
//
//    /**
//     * 根据内容的序列号文本信息，设置内容
//     */
//    public void setContent(String content);

//    /**
//     * 根据广告返回的素材，更新应用对象
//     */
//    public void setExtra(String assets);
}
