package org.qcode.qskinloader.impl;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.qcode.qskinloader.IActivitySkinEventHandler;
import org.qcode.qskinloader.ISkinActivity;
import org.qcode.qskinloader.ISkinAttributeParser;
import org.qcode.qskinloader.IViewCreateListener;
import org.qcode.qskinloader.SkinManager;
import org.qcode.qskinloader.base.utils.Logging;

import java.lang.ref.WeakReference;

/***
 * 支持换肤界面的帮助类
 */
public class ActivitySkinEventHandlerImpl implements IActivitySkinEventHandler {

    private static final String TAG = "ActivityEventHandler";

    private final SkinManagerImpl mSkinManager;

    private volatile boolean mNeedRefreshSkin = false;

    //当前界面是否有Focus
    private boolean mHasFocus;

    //皮肤发生变化时，当前界面是否需要立刻刷新皮肤
    private boolean mSwitchSkinImmediately;

    //当前界面是否支持皮肤变化
    private boolean mIsSupportSkinChange;

    /**
     * VeiZhang
     * 是否默认支持所有的View换肤
     */
    private boolean mIsSupportAllViewSkin = false;

    private WeakReference<Activity> mActivity = null;
    private int mWindowBgResId = -1;
    private SkinInflaterFactoryImpl mSkinInflaterFactoryImpl;
    private IViewCreateListener mViewCreateListener;
    private boolean mNeedDelegateViewCreate = true;

    private SkinAttributeParser mSkinAttributeParser;

    public ActivitySkinEventHandlerImpl() {
        mSkinManager = SkinManagerImpl.getInstance();
    }

    @Override
    public void onCreate(Activity activity) {
        if (!mIsSupportSkinChange) {
            return;
        }

        mActivity = new WeakReference<Activity>(activity);

        if(mNeedDelegateViewCreate) {
            mSkinInflaterFactoryImpl =
                    new SkinInflaterFactoryImpl(getSkinAttributeParser());
            mSkinInflaterFactoryImpl.setViewCreateListener(mViewCreateListener);
            activity.getLayoutInflater().setFactory(mSkinInflaterFactoryImpl);
        }

        mSkinManager.addObserver(this);
    }

    @Override
    public void setViewCreateListener(IViewCreateListener viewCreateListener) {
        mViewCreateListener = viewCreateListener;
        if(null != mSkinInflaterFactoryImpl) {
            mSkinInflaterFactoryImpl.setViewCreateListener(viewCreateListener);
        }
    }

    @Override
    public void onViewCreated() {
        if (!mIsSupportSkinChange) {
            return;
        }

        Logging.d(TAG, "onViewCreated()");

        View contentView = getContentView();
        if (!mSkinManager.getSkinResourceManager().isDefault()) {
            mSkinManager.applySkin(contentView, true);
            refreshWindowBg(contentView);
        }

        if (!mSkinManager.getLanguageResourceManager().isDefault())
        {
            mSkinManager.applyLanguage(contentView, true);
        }

        if (!mSkinManager.getSizeResourceManager().isDefault())
        {
            mSkinManager.applySize(contentView, true);
        }

        mSkinManager.addObserver(this);
    }

    @Override
    public void onStart() {
        //do nothing
    }

    @Override
    public void onResume() {
        //do nothing
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!mIsSupportSkinChange) {
            return;
        }

        mHasFocus = hasFocus;

        if(mHasFocus) {
            if (mNeedRefreshSkin) {
                mNeedRefreshSkin = false;
                //后台界面展示出来时再刷新
                refreshSkin();
                refreshLanguage();
                refreshSize();
            }
        }
    }

    @Override
    public void onPause() {
        //do nothing
    }

    @Override
    public void onStop() {
        //do nothing
    }

    @Override
    public void onDestroy() {
        if (!mIsSupportSkinChange) {
            return;
        }

        mSkinManager.removeObserver(this);
        SkinManager
                .with(getContentView())
                .cleanAttrs(true);

        mActivity.clear();
    }

    @Override
    public IActivitySkinEventHandler setSwitchSkinImmediately(boolean isImmediate) {
        mSwitchSkinImmediately = isImmediate;
        return this;
    }

    @Override
    public IActivitySkinEventHandler setSupportSkinChange(boolean supportChange) {
        mIsSupportSkinChange = supportChange;
        return this;
    }

    @Override
    public IActivitySkinEventHandler setSupportAllViewSkin(boolean supportAllViewSkin) {
        mIsSupportAllViewSkin = supportAllViewSkin;
        return this;
    }

    @Override
    public void handleSkinUpdate() {
        if (!mIsSupportSkinChange) {
            Logging.d(TAG, "onThemeUpdate()| not support theme change: " + getClass().getSimpleName());
            return;
        }

        if (mHasFocus || mSwitchSkinImmediately) {
            mNeedRefreshSkin = false;
            refreshSkin();
        } else {
            //仅置位，不立刻刷新
            mNeedRefreshSkin = true;
        }
    }

    @Override
    public void handleLanguageUpdate() {
        if (!mIsSupportSkinChange) {
            Logging.d(TAG, "onThemeUpdate()| not support theme change: " + getClass().getSimpleName());
            return;
        }

        if (mHasFocus || mSwitchSkinImmediately) {
            mNeedRefreshSkin = false;
            refreshLanguage();
        } else {
            //仅置位，不立刻刷新
            mNeedRefreshSkin = true;
        }
    }

    @Override
    public void handleSizeUpdate() {
        if (!mIsSupportSkinChange) {
            Logging.d(TAG, "onThemeUpdate()| not support theme change: " + getClass().getSimpleName());
            return;
        }

        if (mHasFocus || mSwitchSkinImmediately) {
            mNeedRefreshSkin = false;
            refreshSize();
        } else {
            //仅置位，不立刻刷新
            mNeedRefreshSkin = true;
        }
    }

    @Override
    public ISkinAttributeParser getSkinAttributeParser() {
        if(null == mSkinAttributeParser) {
            mSkinAttributeParser = new SkinAttributeParser(mIsSupportAllViewSkin);
        }

        return mSkinAttributeParser;
    }

    @Override
    public IActivitySkinEventHandler setWindowBackgroundResource(int resId) {
        mWindowBgResId = resId;
        return this;
    }

    @Override
    public IActivitySkinEventHandler setNeedDelegateViewCreate(boolean needDelegateViewCreate) {
        mNeedDelegateViewCreate = needDelegateViewCreate;
        return this;
    }

    private void refreshSkin() {
        if (!mIsSupportSkinChange) {
            return;
        }

        if (null == mActivity) {
            return;
        }

        final Activity activity = mActivity.get();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View contentView = getContentView();
                mSkinManager.applySkin(contentView, true);
                mSkinManager.applyWindowViewSkin();
                refreshWindowBg(contentView);

                //通知Activity做其他刷新操作
                if (activity instanceof ISkinActivity) {
                    ((ISkinActivity) activity).handleSkinChange();
                }
            }
        });
    }

    /**
     * VeiZhang
     * 刷新语言
     */
    private void refreshLanguage() {
        if (!mIsSupportSkinChange) {
            return;
        }

        if (null == mActivity) {
            return;
        }

        final Activity activity = mActivity.get();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View contentView = getContentView();
                mSkinManager.applyLanguage(contentView, true);
                mSkinManager.applyWindowViewLanguage();
                //通知Activity做其他刷新操作
                if (activity instanceof ISkinActivity) {
                    ((ISkinActivity) activity).handleLanguageChange();
                }
            }
        });
    }

    /**
     * VeiZhang
     * 刷新字体大小
     */
    private void refreshSize() {
        if (!mIsSupportSkinChange) {
            return;
        }

        if (null == mActivity) {
            return;
        }

        final Activity activity = mActivity.get();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View contentView = getContentView();
                mSkinManager.applySize(contentView, true);
                mSkinManager.applyWindowViewSize();
                //通知Activity做其他刷新操作
                if (activity instanceof ISkinActivity) {
                    ((ISkinActivity) activity).handleSizeChange();
                }
            }
        });
    }

    private void refreshWindowBg(View contentView) {
        if(!mIsSupportSkinChange) {
            return;
        }

        if (mWindowBgResId <= 0) {
            return;
        }

        if (null == mActivity) {
            return;
        }
        Activity activity = mActivity.get();
        if (null == activity) {
            return;
        }

        Drawable bgDrawable;
        try {
            bgDrawable = new ColorDrawable(
                    mSkinManager.getSkinResourceManager().getColor(mWindowBgResId));

        } catch (Resources.NotFoundException ex) {
            try {
                bgDrawable = mSkinManager.getSkinResourceManager().getDrawable(mWindowBgResId);
            } catch (Resources.NotFoundException e) {
                return;
            }
        }
//        contentView.setBackgroundDrawable(bgDrawable);
        activity.getWindow().setBackgroundDrawable(bgDrawable);
    }

    public View getContentView() {
        if (null == mActivity) {
            return null;
        }

        Activity activity = mActivity.get();
        if (null == activity) {
            return null;
        }

        return activity.findViewById(android.R.id.content);
    }
}
