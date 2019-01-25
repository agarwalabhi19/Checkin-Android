package com.checkin.app.checkin.Session.ActiveSession;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.checkin.app.checkin.Data.Resource;
import com.checkin.app.checkin.R;
import com.checkin.app.checkin.Session.Model.SessionOrderedItemModel;
import com.checkin.app.checkin.Utility.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActiveSessionViewOrdersActivity extends AppCompatActivity implements ActiveSessionOrdersAdapter.SessionOrdersInteraction {
    @BindView(R.id.rv_active_session_orders)
    RecyclerView rvOrders;
    private ActiveSessionOrdersAdapter mOrdersAdapter;
    private ActiveSessionOrdersViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_session_view_orders);
        ButterKnife.bind(this);

        rvOrders.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mOrdersAdapter = new ActiveSessionOrdersAdapter(null, this);
        rvOrders.setAdapter(mOrdersAdapter);

        mViewModel = ViewModelProviders.of(this).get(ActiveSessionOrdersViewModel.class);

        mViewModel.getSessionOrdersData().observe(this, listResource -> {
            if (listResource != null && listResource.status == Resource.Status.SUCCESS)
                mOrdersAdapter.setData(listResource.data);
        });

        mViewModel.getObservableData().observe(this, resource -> {
            if (resource == null)
                return;
            switch (resource.status) {
                case SUCCESS: {
                    Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();
                    mViewModel.getSessionOrdersData().observe(this, listResource -> {
                        if (listResource != null && listResource.status == Resource.Status.SUCCESS)
                            mOrdersAdapter.setData(listResource.data);
                    });
                    break;
                }
                case LOADING:
                    break;
                default: {
                    Utils.toast(this, resource.message);
                }
            }
        });
    }

    @Override
    public void onCancelOrder(SessionOrderedItemModel orderedItem) {
        mViewModel.deleteSessionOrder(String.valueOf(orderedItem.getPk()));
    }
}