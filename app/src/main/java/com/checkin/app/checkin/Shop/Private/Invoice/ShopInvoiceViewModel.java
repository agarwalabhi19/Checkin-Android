package com.checkin.app.checkin.Shop.Private.Invoice;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.checkin.app.checkin.Data.BaseViewModel;
import com.checkin.app.checkin.Data.Resource;
import com.checkin.app.checkin.Utility.SourceMappedLiveData;

import java.util.List;

public class ShopInvoiceViewModel extends BaseViewModel {
    private ShopInvoiceRepository mShopInvoiceRepository;

    private long mShopPk;
    private String fromDate, toDate;
    private LiveData<Resource<List<RestaurantSessionModel>>> mPrevResults;
    private SourceMappedLiveData<Resource<List<RestaurantSessionModel>>> mResults = createNetworkLiveData();

    public ShopInvoiceViewModel(@NonNull Application application) {
        super(application);
        this.mShopInvoiceRepository = ShopInvoiceRepository.getInstance(application);
    }

    public void fetchShopSessions(long restaurantId) {
        this.mShopPk = restaurantId;
        mPrevResults = mShopInvoiceRepository.getRestaurantSessions(restaurantId);
        mResults.addSource(mPrevResults, mResults::setValue);
    }

    public void setShopPk(long restaurantId) {
        this.mShopPk = restaurantId;
    }

    public void filterRestaurantSessions(String fromDate, String toDate) {
        if (mPrevResults != null)
            mResults.removeSource(mPrevResults);
        mPrevResults = mShopInvoiceRepository.getRestaurantSessions(mShopPk, fromDate, toDate);
        mResults.addSource(mPrevResults, mResults::setValue);
    }

    public void filterFrom(String fromDate) {
        this.fromDate = fromDate;
        filterRestaurantSessions(fromDate, toDate);
    }

    public void filterTo(String toDate) {
        this.toDate = toDate;
        filterRestaurantSessions(fromDate, toDate);
    }

    public LiveData<Resource<List<RestaurantSessionModel>>> getRestaurantSessions() {
        return mResults;
    }

    @Override
    public void updateResults() {
        fetchShopSessions(mShopPk);
    }
}
