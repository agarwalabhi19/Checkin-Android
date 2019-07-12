package com.checkin.app.checkin.session.activesession;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.checkin.app.checkin.Data.Message.MessageModel;
import com.checkin.app.checkin.Data.Message.MessageObjectModel;
import com.checkin.app.checkin.Data.Message.MessageUtils;
import com.checkin.app.checkin.Data.ProblemModel;
import com.checkin.app.checkin.Data.Resource;
import com.checkin.app.checkin.Misc.BaseActivity;
import com.checkin.app.checkin.Misc.BillHolder;
import com.checkin.app.checkin.Misc.paytm.PaytmPayment;
import com.checkin.app.checkin.R;
import com.checkin.app.checkin.Shop.ShopModel;
import com.checkin.app.checkin.User.bills.SuccessfulTransactionActivity;
import com.checkin.app.checkin.Utility.Constants;
import com.checkin.app.checkin.Utility.Utils;
import com.checkin.app.checkin.session.model.PromoDetailModel;
import com.checkin.app.checkin.session.model.SessionBillModel;
import com.checkin.app.checkin.session.model.SessionInvoiceModel;
import com.checkin.app.checkin.session.model.SessionPromoModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.checkin.app.checkin.Shop.ShopModel.PAYMENT_MODE.PAYTM;
import static com.checkin.app.checkin.session.activesession.ActiveSessionPaymentOptionsFragment.KEY_PAYMENT_MODE_RESULT;
import static com.checkin.app.checkin.session.activesession.ActiveSessionPaymentOptionsFragment.KEY_SESSION_AMOUNT;

public class ActiveSessionInvoiceActivity extends BaseActivity implements ActiveSessionPaymentOptionsFragment.OnSelectPaymentMode {
    public static final String KEY_SESSION_REQUESTED_CHECKOUT = "invoice.session.requested_checkout";
    private static final int REQUEST_PAYMENT_MODE = 141;

    @BindView(R.id.rv_invoice_ordered_items)
    RecyclerView rvOrderedItems;
    @BindView(R.id.im_invoice_waiter)
    ImageView imWaiterPic;
    @BindView(R.id.tv_invoice_tip)
    TextView tvInvoiceTip;
    @BindView(R.id.tv_invoice_total)
    TextView tvInvoiceTotal;
    @BindView(R.id.tv_as_payment_mode)
    TextView tvPaymentMode;
    @BindView(R.id.ed_invoice_tip)
    EditText edInvoiceTip;
    @BindView(R.id.im_invoice_remove_promo_code)
    ImageView removePromoCode;
    @BindView(R.id.tv_as_promo_applied_details)
    TextView tvAppliedPromoDetails;
    @BindView(R.id.tv_as_promo_invalid_status)
    TextView tvPromoInvalidStatus;
    @BindView(R.id.container_remove_promo_code)
    ViewGroup containerRemovePromo;
    @BindView(R.id.container_promo_code_apply)
    ViewGroup containerApplyPromo;
    @BindView(R.id.container_invoice_tip_waiter)
    ViewGroup containerTipWaiter;
    @BindView(R.id.btn_invoice_request_checkout)
    Button btnRequestCheckout;
    @BindView(R.id.container_as_session_benefits)
    ViewGroup containerSessionBenefits;
    @BindView(R.id.tv_as_session_benefits)
    TextView tvSessionBenefits;
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;

    private long sessionId;

    private ActiveSessionInvoiceViewModel mViewModel;
    private InvoiceOrdersAdapter mAdapter;
    private SessionBillModel mBillModel;
    private BillHolder mBillHolder;
    private SharedPreferences mPrefs;
    private PaytmPayment mPaymentPayTm;
    private ActiveSessionPromoFragment mPromoFragment;
    private ActiveSessionPaymentOptionsFragment mPaymentFragment;

    private ShopModel.PAYMENT_MODE selectedMode;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MessageModel message = MessageUtils.parseMessage(intent);
            if (message == null) return;

            MessageObjectModel model;

            switch (message.getType()) {
                case USER_SESSION_PROMO_AVAILED:
                case USER_SESSION_PROMO_REMOVED:
                    mViewModel.fetchSessionAppliedPromo();
                    break;
                case USER_SESSION_BILL_CHANGE:
                    mViewModel.fetchSessionInvoice();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_session_invoice);
        ButterKnife.bind(this);

        initBackButton(R.id.tv_header_title, getResources().getString(R.string.action_invoice));
        initProgressBar(R.id.pb_as_checkout);

        mViewModel = ViewModelProviders.of(this).get(ActiveSessionInvoiceViewModel.class);

        setupUi();
        getData();
        setupObserver();
        paytmObserver();
    }

    private void setupUi() {
        initRefreshScreen(R.id.sr_active_session_invoice);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String paymentTag = mPrefs.getString(Constants.SP_LAST_USED_PAYMENT_MODE, PAYTM.tag);
        selectedMode = ShopModel.PAYMENT_MODE.getByTag(paymentTag);

        setPaymentModeUpdates();
        rvOrderedItems.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mAdapter = new InvoiceOrdersAdapter(null, null);
        rvOrderedItems.setAdapter(mAdapter);
        mBillHolder = new BillHolder(findViewById(android.R.id.content));

        mPromoFragment = ActiveSessionPromoFragment.newInstance();
        mPaymentFragment = ActiveSessionPaymentOptionsFragment.newInstance(this);
    }

    private void setPaymentModeUpdates() {
        tvPaymentMode.setText(ShopModel.getPaymentMode(selectedMode));
        tvPaymentMode.setCompoundDrawablesWithIntrinsicBounds(ShopModel.getPaymentModeIcon(selectedMode), 0, 0, 0);
        if (selectedMode != null && selectedMode.equals(PAYTM))
            btnRequestCheckout.setText(R.string.title_pay_checkout);
        else {
//            showPromoInvalid(R.string.label_session_offer_not_allowed_payment_mode_cash);
            btnRequestCheckout.setText(R.string.title_request_checkout);
        }
    }

    private void getData() {
        mViewModel.fetchSessionInvoice();
        mViewModel.fetchAvailablePromoCodes();
        mViewModel.fetchSessionAppliedPromo();

        boolean isRequestedCheckout = getIntent().getBooleanExtra(KEY_SESSION_REQUESTED_CHECKOUT, false);
        if (isRequestedCheckout && !mViewModel.isRequestedCheckout())
            mViewModel.updateRequestCheckout(true);
    }

    private void setupData(SessionInvoiceModel data) {
        mAdapter.setData(data.getOrderedItems());
        mBillModel = data.getBill();

        if (data.getHost() != null)
            Utils.loadImageOrDefault(imWaiterPic, data.getHost().getDisplayPic(), R.drawable.ic_waiter);
        else
            containerTipWaiter.setVisibility(View.GONE);

        mBillHolder.bind(data.getBill());
        edInvoiceTip.setText(data.getBill().formatTip());
        tvInvoiceTotal.setText(Utils.formatCurrencyAmount(this, data.getBill().getTotal()));
    }

    private void setupObserver() {
        mViewModel.getSessionInvoice().observe(this, resource -> {
            if (resource == null)
                return;
            if (resource.getStatus() == Resource.Status.SUCCESS && resource.getData() != null) {
                setupData(resource.getData());
                tryShowTotalSavings();
                sessionId = resource.getData().getPk();
            }

            if (resource.getStatus() != Resource.Status.LOADING)
                stopRefreshing();
        });

        mViewModel.getCheckoutData().observe(this, statusModelResource -> {
            if (statusModelResource == null)
                return;

            if (statusModelResource.getStatus() == Resource.Status.SUCCESS && statusModelResource.getData() != null) {
                Utils.toast(this, statusModelResource.getData().getMessage());
                if (statusModelResource.getData().isCheckout())
                    Utils.navigateBackToHome(getApplicationContext());
                else {
                    mViewModel.updateRequestCheckout(true);
                    selectedMode = ShopModel.PAYMENT_MODE.getByTag(statusModelResource.getData().getPaymentMode());
                    switch (selectedMode) {
                        case PAYTM:
                            mViewModel.requestPaytmDetails();
                            break;
                        case CASH:
                            finish();
                            break;
                        default:
                            break;
                    }
                }
            } else if (statusModelResource.getStatus() != Resource.Status.LOADING) {
                Utils.toast(ActiveSessionInvoiceActivity.this, statusModelResource.getMessage());
                hideProgressBar();
                if (statusModelResource.getProblem() != null && statusModelResource.getProblem().getErrorCode() == ProblemModel.ERROR_CODE.INVALID_PAYMENT_MODE_PROMO_AVAILED)
                    onPaymentModeClick();
            }
        });

        mViewModel.getRequestedCheckout().observe(this, isRequestedCheckout -> {
            edInvoiceTip.setEnabled(!isRequestedCheckout);

            if (isRequestedCheckout) {
                edInvoiceTip.setBackground(getResources().getDrawable(R.drawable.bordered_text_light_grey));
                edInvoiceTip.setPadding(15, 0, 0, 0);
                btnRequestCheckout.setText(R.string.session_inform_requested_checkout);
                showPromoInvalid(R.string.label_session_offer_not_allowed_session_requested_checkout);
            } else {
                setPaymentModeUpdates();
            }
        });

        mViewModel.getPromoDeletedData().observe(this, resource -> {
            if (resource == null)
                return;
            if (resource.getStatus() == Resource.Status.SUCCESS) {
                showPromoApply();
                tryShowTotalSavings();
            }
            Utils.toast(this, resource.getMessage());
        });

        mViewModel.getSessionAppliedPromo().observe(this, sessionPromoModelResource -> {
            if (sessionPromoModelResource == null)
                return;
            if (sessionPromoModelResource.getStatus() == Resource.Status.SUCCESS && sessionPromoModelResource.getData() != null) {
                showPromoDetails(sessionPromoModelResource.getData());
                tryShowTotalSavings();
            } else if (sessionPromoModelResource.getStatus() == Resource.Status.ERROR_NOT_FOUND) {
                showPromoApply();
            }
        });

        mViewModel.getPromoCodes().observe(this, listResource -> {
            if (listResource == null)
                return;
            if (listResource.getStatus() == Resource.Status.SUCCESS && listResource.getData() != null && listResource.getData().size() > 0) {
                tryShowAvailableOffer(listResource.getData().get(0));
            } else if (listResource.getStatus() == Resource.Status.ERROR_FORBIDDEN) {
                showPromoInvalid(R.string.label_session_offer_not_allowed_phone_not_registered);
            } else if (listResource.getStatus() != Resource.Status.LOADING) {
                Utils.toast(this, listResource.getMessage());
            }
        });
    }

    private void tryShowAvailableOffer(PromoDetailModel promoDetailModel) {
        if (!mViewModel.isSessionBenefitsShown())
            showSessionBenefit(String.format("Offer available! %s", promoDetailModel.getName()));
    }

    private void tryShowTotalSavings() {
        double savings = 0;
        Resource<SessionInvoiceModel> invoiceModelResource = mViewModel.getSessionInvoice().getValue();
        if (invoiceModelResource != null && invoiceModelResource.getData() != null) {
            savings = invoiceModelResource.getData().getBill().getTotalSaving();
        } else {
            Resource<SessionPromoModel> promoModelResource = mViewModel.getSessionAppliedPromo().getValue();
            if (promoModelResource != null && promoModelResource.getData() != null) {
                savings = promoModelResource.getData().getOfferAmount();
            }
        }
        if (savings > 0)
            showSessionBenefit(String.format(getString(R.string.format_session_benefits_savings), savings));
        else
            hideSessionBenefit();
    }

    private void showPromoInvalid(@StringRes int errorMsg) {
        if (mViewModel.isOfferApplied())
            return;
        resetPromoCards();
        tvPromoInvalidStatus.setVisibility(View.VISIBLE);
        tvPromoInvalidStatus.setText(errorMsg);
        tvPromoInvalidStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_error_exclamation, 0, 0, 0);
        mViewModel.setSessionPromoInvalid(true);
    }

    private void showPromoApply() {
        if (mViewModel.isSessionPromoInvalid())
            return;
        resetPromoCards();
        containerApplyPromo.setVisibility(View.VISIBLE);
    }

    private void showPromoDetails(SessionPromoModel data) {
        resetPromoCards();
        containerRemovePromo.setVisibility(View.VISIBLE);
        tvAppliedPromoDetails.setText(data.getDetails());
    }

    private void paytmObserver() {
        mPaymentPayTm = new PaytmPayment() {
            @Override
            protected void onPaytmTransactionResponse(Bundle inResponse) {
                mViewModel.postPaytmCallback(inResponse);
            }

            @Override
            protected void onPaytmTransactionCancel(Bundle inResponse, String msg) {
                mViewModel.cancelCheckoutRequest();
                Utils.toast(ActiveSessionInvoiceActivity.this, msg);
            }

            @Override
            protected void onPaytmError(String inErrorMessage) {
                mViewModel.cancelCheckoutRequest();
                Utils.toast(ActiveSessionInvoiceActivity.this, inErrorMessage);
            }
        };

        mViewModel.getPaytmDetails().observe(this, paytmModelResource -> {
            if (paytmModelResource == null)
                return;
            if (paytmModelResource.getStatus() == Resource.Status.SUCCESS && paytmModelResource.getData() != null) {
                mPaymentPayTm.initializePayment(paytmModelResource.getData(), this);
            } else if (paytmModelResource.getStatus() != Resource.Status.LOADING) {
                Utils.toast(this, paytmModelResource.getMessage());
            }
        });

        mViewModel.getPaytmCallbackData().observe(this, objectNodeResource -> {
            if (objectNodeResource == null)
                return;
            if (objectNodeResource.getStatus() == Resource.Status.SUCCESS) {
//                Utils.navigateBackToHome(this);
                Intent successIntent = new Intent(this, SuccessfulTransactionActivity.class);
                successIntent.putExtra(SuccessfulTransactionActivity.KEY_SESSION_ID, sessionId);
                startActivity(successIntent);
                finish();
            }
            if (objectNodeResource.getStatus() != Resource.Status.LOADING) {
                Utils.toast(this, objectNodeResource.getMessage());
                hideProgressBar();
            }
        });

        mViewModel.getSessionCancelCheckoutData().observe(this, objectNodeResource -> {
            if (objectNodeResource == null)
                return;
            if (objectNodeResource.getStatus() == Resource.Status.SUCCESS) {
                mViewModel.updateRequestCheckout(false);
            }
            if (objectNodeResource.getStatus() != Resource.Status.LOADING) {
                hideProgressBar();
            }
        });
    }

    @OnTextChanged(value = R.id.ed_invoice_tip, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTipChange(Editable editable) {
        double amount = 0d;
        try {
            amount = Double.parseDouble(editable.toString());
        } catch (NumberFormatException ignored) {
        }
        if (mBillModel != null) {
            mBillModel.giveTip(amount);
            tvInvoiceTip.setText(Utils.formatCurrencyAmount(this, mBillModel.getTip()));
            tvInvoiceTotal.setText(Utils.formatCurrencyAmount(this, mBillModel.getTotal()));
        }
    }

    @OnClick(R.id.btn_invoice_request_checkout)
    public void onClickCheckout() {
        if (mViewModel.isRequestedCheckout())
            alertDialogForOverridingPaymentRequest();
        else
            onRequestCheckout(false);
    }

    @OnClick(R.id.container_promo_code_apply)
    public void onPromoCodeClick() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_promo, mPromoFragment)
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.im_invoice_remove_promo_code)
    public void onRemovePromoCode() {
        mViewModel.removePromoCode();
    }

    private void onRequestCheckout(boolean override) {
        if (selectedMode != null) {
            mViewModel.requestCheckout(mBillModel.getTip(), selectedMode, override);
            visibleProgressBar();
        } else
            Utils.toast(this, "Please select the Payment Mode.");
    }

    @OnClick(R.id.container_as_payment_mode_change)
    public void onPaymentModeClick() {
//        tvHeaderTitle.setText(getResources().getString(R.string.action_payment_mode));

        Bundle bundle = new Bundle();
        bundle.putString(KEY_SESSION_AMOUNT, tvInvoiceTotal.getText().toString());
        mPaymentFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_promo, mPaymentFragment)
                .addToBackStack(null)
                .commit();
    }

    @OnClick(R.id.im_header_back)
    public void onBackClick() {
        onBackPressed();
    }

    @Override
    public void paymentSelect(ShopModel.PAYMENT_MODE paymentMode) {
        if(paymentMode == null)
            return;
        selectedMode = paymentMode;
        mPrefs.edit()
                .putString(Constants.SP_LAST_USED_PAYMENT_MODE, selectedMode.tag)
                .apply();

        setPaymentModeUpdates();
        tvPaymentMode.setCompoundDrawablesWithIntrinsicBounds(ShopModel.getPaymentModeIcon(selectedMode), 0, 0, 0);
    }

    private void showSessionBenefit(String msg) {
        containerSessionBenefits.setVisibility(View.VISIBLE);
        tvSessionBenefits.setText(Html.fromHtml(msg));
        mViewModel.showedSessionBenefits();
    }

    private void hideSessionBenefit() {
        containerSessionBenefits.setVisibility(View.GONE);
    }

    private void resetPromoCards() {
        containerRemovePromo.setVisibility(View.GONE);
        containerApplyPromo.setVisibility(View.GONE);
        tvPromoInvalidStatus.setVisibility(View.GONE);
        tvPromoInvalidStatus.setText(R.string.active_session_fetching_offers);
        tvPromoInvalidStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    private void alertDialogForOverridingPaymentRequest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Override checkout request?")
                .setMessage("A payment request is in progress. Are you sure you want to cancel that request and initiate a new one?")
                .setPositiveButton("Yes", (dialog, which) -> onRequestCheckout(true))
                .setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessageModel.MESSAGE_TYPE[] types = new MessageModel.MESSAGE_TYPE[]{
                MessageModel.MESSAGE_TYPE.USER_SESSION_PROMO_AVAILED, MessageModel.MESSAGE_TYPE.USER_SESSION_PROMO_REMOVED,
                MessageModel.MESSAGE_TYPE.USER_SESSION_BILL_CHANGE};
        MessageUtils.registerLocalReceiver(this, mReceiver, types);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MessageUtils.unregisterLocalReceiver(this, mReceiver);
    }

    @Override
    protected void updateScreen() {
        mViewModel.updateResults();
    }
}
