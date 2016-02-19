/*
 * Copyright (C) 2016 Doodle AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doodle.android.chips;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doodle.android.chips.dialog.ChipsEmailDialogFragment;
import com.doodle.android.chips.model.Contact;
import com.doodle.android.chips.util.Common;
import com.doodle.android.chips.views.ChipsEditText;
import com.doodle.android.chips.views.ChipsVerticalLinearLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChipsView extends RelativeLayout implements ChipsEditText.InputConnectionWrapperInterface, ChipsEmailDialogFragment.EmailListener {

    private static final String TAG = "ChipsView";

    private static final int CHIP_HEIGHT = 33;
    private static final int TEXT_EXTRA_TOP_MARGIN = 4;
    public static final int CHIP_BOTTOM_PADDING = 1;

    // RES --------------------------------------------------

    private int mChipsBgRes = R.drawable.chip_background;

    // ------------------------------------------------------

    private int mChipsColor;
    private int mChipsColorClicked;
    private int mChipsColorErrorClicked;
    private int mChipsBgColor;
    private int mChipsBgColorClicked;
    private int mChipsBgColorErrorClicked;
    private int mChipsTextColor;
    private int mChipsTextColorClicked;
    private int mChipsTextColorErrorClicked;
    private int mChipsPlaceholderResId;
    private int mChipsDeleteResId;

    private String mChipsDialogTitle;
    private String mChipsDialogPlaceholder;
    private String mChipsDialogConfirm;
    private String mChipsDialogCancel;
    private String mChipsDialogErrorMsg;

    // ------------------------------------------------------

    private float mDensity;

    private ChipsListener mChipsListener;

    private ChipsEditText mEditText;
    private ChipsVerticalLinearLayout mRootChipsLayout;

    private EditTextListener mEditTextListener;

    private List<Chip> mChipList = new ArrayList<>();

    private Object mCurrentEditTextSpan;

    private ChipValidator mChipsValidator;

    public ChipsView(Context context) {
        super(context);
        init();
    }

    public ChipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init();
    }

    public ChipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChipsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs);
        init();
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ChipsView,
                0, 0);
        try {
            mChipsColor = a.getColor(R.styleable.ChipsView_cv_color,
                    ContextCompat.getColor(context, R.color.base30));
            mChipsColorClicked = a.getColor(R.styleable.ChipsView_cv_color_clicked,
                    ContextCompat.getColor(context, R.color.colorPrimaryDark));
            mChipsColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_color_error_clicked,
                    ContextCompat.getColor(context, R.color.color_error));

            mChipsBgColor = a.getColor(R.styleable.ChipsView_cv_bg_color,
                    ContextCompat.getColor(context, R.color.base10));
            mChipsBgColorClicked = a.getColor(R.styleable.ChipsView_cv_bg_color_clicked,
                    ContextCompat.getColor(context, R.color.blue));

            mChipsBgColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_bg_color_clicked,
                    ContextCompat.getColor(context, R.color.color_error));

            mChipsTextColor = a.getColor(R.styleable.ChipsView_cv_text_color,
                    Color.BLACK);
            mChipsTextColorClicked = a.getColor(R.styleable.ChipsView_cv_text_color_clicked,
                    Color.WHITE);
            mChipsTextColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_text_color_clicked,
                    Color.WHITE);

            mChipsPlaceholderResId = a.getResourceId(R.styleable.ChipsView_cv_icon_placeholder,
                    R.drawable.ic_person_24dp);
            mChipsDeleteResId = a.getResourceId(R.styleable.ChipsView_cv_icon_delete,
                    R.drawable.ic_close_24dp);

            mChipsDialogTitle = a.getString(R.styleable.ChipsView_cv_dialog_title);
            if (TextUtils.isEmpty(mChipsDialogTitle)) {
                mChipsDialogTitle = getResources().getString(R.string.chips_enter_email_address);
            }
            mChipsDialogPlaceholder = a.getString(R.styleable.ChipsView_cv_dialog_et_placeholder);
            if (TextUtils.isEmpty(mChipsDialogPlaceholder)) {
                mChipsDialogPlaceholder = getResources().getString(R.string.email);
            }
            mChipsDialogConfirm = a.getString(R.styleable.ChipsView_cv_dialog_confirm);
            if (TextUtils.isEmpty(mChipsDialogConfirm)) {
                mChipsDialogConfirm = getResources().getString(R.string.confirm);
            }
            mChipsDialogCancel = a.getString(R.styleable.ChipsView_cv_dialog_cancel);
            if (TextUtils.isEmpty(mChipsDialogCancel)) {
                mChipsDialogCancel = getResources().getString(R.string.cancel);
            }
            mChipsDialogErrorMsg = a.getString(R.styleable.ChipsView_cv_dialog_error_msg);
            if (TextUtils.isEmpty(mChipsDialogErrorMsg)) {
                mChipsDialogErrorMsg = getResources().getString(R.string.please_enter_a_valid_email_address);
            }


        } finally {
            a.recycle();
        }
    }

    private void init() {
        mDensity = getResources().getDisplayMetrics().density;

        // Dummy item to prevent AutoCompleteTextView from receiving focus
        LinearLayout linearLayout = new LinearLayout(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        linearLayout.setLayoutParams(params);
        linearLayout.setFocusable(true);
        linearLayout.setFocusableInTouchMode(true);

        addView(linearLayout);

        mEditText = new ChipsEditText(getContext(), this);
        mEditText.setBackgroundColor(Color.argb(0, 0, 0, 0));
        mEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_UNSPECIFIED);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //mEditText.setHint(R.string.name_or_email_address);

        addView(mEditText);

        mRootChipsLayout = new ChipsVerticalLinearLayout(getContext());
        mRootChipsLayout.setOrientation(LinearLayout.VERTICAL);
        mRootChipsLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mRootChipsLayout);

        initListener();
    }

    private void initListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.requestFocus();
            }
        });

        mEditTextListener = new EditTextListener();
        mEditText.addTextChangedListener(mEditTextListener);
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    unSelectChipsExcept(null);
                }
            }
        });
    }

    public void addChip(String displayName, String avatarUrl, Contact contact) {
        addChip(displayName, Uri.parse(avatarUrl), contact);
    }

    public void addChip(String displayName, Uri avatarUrl, Contact contact) {
        addChip(displayName, avatarUrl, contact, false);
        mEditText.setText("");
        addLeadingMarginSpan();
    }

    public void addChip(String displayName, Uri avatarUrl, Contact contact, boolean isIndelible) {
        Chip chip = new Chip(displayName, avatarUrl, contact, isIndelible);
        mChipList.add(chip);
        if (mChipsListener != null) {
            mChipsListener.onChipAdded(chip);
        }

        onChipsChanged(true);
    }

    public boolean removeChipBy(Contact contact) {
        for (int i = 0; i < mChipList.size(); i++) {
            if (mChipList.get(i).mContact != null && mChipList.get(i).mContact.equals(contact)) {
                mChipList.remove(i);
                onChipsChanged(true);
                return true;
            }
        }
        return false;
    }

    public Contact tryToRecognizeAddress() {
        String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (Common.isValidEmail(text)) {
                return new Contact(text, "", null, text, null);
            }
        }
        return null;
    }

    /**
     * rebuild all chips and place them right
     */
    private void onChipsChanged(final boolean moveCursor) {
        ChipsVerticalLinearLayout.TextLineParams textLineParams = mRootChipsLayout.onChipsChanged(mChipList);

        // if null then run another layout pass
        if (textLineParams == null) {
            post(new Runnable() {
                @Override
                public void run() {
                    onChipsChanged(moveCursor);
                }
            });
            return;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (textLineParams.row * CHIP_HEIGHT * mDensity + TEXT_EXTRA_TOP_MARGIN * mDensity);
        mEditText.setLayoutParams(params);
        addLeadingMarginSpan(textLineParams.lineMargin);
        if (moveCursor) {
            mEditText.setSelection(mEditText.length());
        }
    }

    private void addLeadingMarginSpan(int margin) {
        Spannable spannable = mEditText.getText();
        if (mCurrentEditTextSpan != null) {
            spannable.removeSpan(mCurrentEditTextSpan);
        }
        mCurrentEditTextSpan = new android.text.style.LeadingMarginSpan.LeadingMarginSpan2.Standard(margin, 0);
        spannable.setSpan(mCurrentEditTextSpan, 0, 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        mEditText.setText(spannable);
    }

    private void addLeadingMarginSpan() {
        Spannable spannable = mEditText.getText();
        if (mCurrentEditTextSpan != null) {
            spannable.removeSpan(mCurrentEditTextSpan);
        }
        spannable.setSpan(mCurrentEditTextSpan, 0, 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        mEditText.setText(spannable);
    }

    private void onEnterPressed(String text) {
        if (text != null && text.length() > 0) {

            if (Common.isValidEmail(text)) {
                onEmailRecognized(text);
            } else {
                onNonEmailRecognized(text);
            }
            mEditText.setSelection(0);
        }
    }

    private void onEmailRecognized(String email) {
        onEmailRecognized(new Contact(email, "", null, email, null));
    }

    private void onEmailRecognized(Contact contact) {
        Chip chip = new Chip(contact.getDisplayName(), null, contact);
        mChipList.add(chip);
        if (mChipsListener != null) {
            mChipsListener.onChipAdded(chip);
        }
        post(new Runnable() {
            @Override
            public void run() {
                onChipsChanged(true);
            }
        });
    }

    private void onNonEmailRecognized(String text) {
        try {
            FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();

            Bundle bundle = new Bundle();
            bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_TEXT, text);
            bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_TITLE, mChipsDialogTitle);
            bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_PLACEHOLDER, mChipsDialogPlaceholder);
            bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_CONFIRM, mChipsDialogConfirm);
            bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_CANCEL, mChipsDialogCancel);
            bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_ERROR_MSG, mChipsDialogErrorMsg);

            ChipsEmailDialogFragment chipsEmailDialogFragment = new ChipsEmailDialogFragment();
            chipsEmailDialogFragment.setArguments(bundle);
            chipsEmailDialogFragment.setEmailListener(this);
            chipsEmailDialogFragment.show(fragmentManager, ChipsEmailDialogFragment.class.getSimpleName());
        } catch (ClassCastException e) {
            Log.e(TAG, "Error ClassCast", e);
        }
    }

    private void selectOrDeleteLastChip() {
        if (mChipList.size() > 0) {
            onChipInteraction(mChipList.size() - 1);
        }
    }

    private void onChipInteraction(int position) {
        try {
            Chip chip = mChipList.get(position);
            if (chip != null) {
                onChipInteraction(chip, true);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Out of bounds", e);
        }
    }

    private void onChipInteraction(Chip chip, boolean nameClicked) {
        unSelectChipsExcept(chip);
        if (chip.isSelected()) {
            mChipList.remove(chip);
            if (mChipsListener != null) {
                mChipsListener.onChipDeleted(chip);
            }
            onChipsChanged(true);
            if (nameClicked) {
                mEditText.setText(chip.getContact().getEmailAddress());
                addLeadingMarginSpan();
                mEditText.requestFocus();
                mEditText.setSelection(mEditText.length());
            }
        } else {
            chip.setSelected(true);
            onChipsChanged(false);
        }
    }

    private void unSelectChipsExcept(Chip rootChip) {
        for (Chip chip : mChipList) {
            if (chip != rootChip) {
                chip.setSelected(false);
            }
        }
        onChipsChanged(false);
    }

    @Override
    public InputConnection getInputConnection(InputConnection target) {
        return new KeyInterceptingInputConnection(target);
    }

    public void setChipsListener(ChipsListener chipsListener) {
        this.mChipsListener = chipsListener;
    }

    @Override
    public void onDialogEmailEntered(String email, String initialText) {
        onEmailRecognized(new Contact(initialText, "", initialText, email, null));
    }

    /**
     * sets the ChipsValidator.
     */
    public void setChipsValidator(ChipValidator mChipsValidator) {
        this.mChipsValidator = mChipsValidator;
    }

    public EditText getEditText() {
        return mEditText;
    }

    private class EditTextListener implements TextWatcher {

        private boolean mIsPasteTextChange = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 1) {
                mIsPasteTextChange = true;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mIsPasteTextChange) {
                mIsPasteTextChange = false;
                // todo handle copy/paste text here

            } else {
                // no paste text change
                if (s.toString().contains("\n")) {
                    String text = s.toString();
                    text = text.replace("\n", "");
                    while (text.contains("  ")) {
                        text = text.replace("  ", " ");
                    }
                    s.clear();
                    if (text.length() > 1) {
                        onEnterPressed(text);
                    } else {
                        s.append(text);
                    }
                }
            }
            if (mChipsListener != null) {
                mChipsListener.onTextChanged(s);
            }
        }
    }

    private class KeyInterceptingInputConnection extends InputConnectionWrapper {

        public KeyInterceptingInputConnection(InputConnection target) {
            super(target, true);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (mEditText.length() == 0) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        selectOrDeleteLastChip();
                        return true;
                    }
                }
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                mEditText.append("\n");
                return true;
            }

            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (mEditText.length() == 0 && beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    public class Chip implements OnClickListener {

        private static final int MAX_LABEL_LENGTH = 30;

        private String mLabel;
        private final Uri mPhotoUri;
        private final Contact mContact;
        private final boolean mIsIndelible;

        private RelativeLayout mView;
        private View mIconWrapper;
        private TextView mTextView;

        private ImageView mAvatarView;
        private ImageView mPersonIcon;
        private ImageView mCloseIcon;

        private ImageView mErrorIcon;

        private boolean mIsSelected = false;

        public Chip(String label, Uri photoUri, Contact contact) {
            this(label, photoUri, contact, false);
        }

        public Chip(String label, Uri photoUri, Contact contact, boolean isIndelible) {
            this.mLabel = label;
            this.mPhotoUri = photoUri;
            this.mContact = contact;
            this.mIsIndelible = isIndelible;

            if (mLabel == null) {
                mLabel = contact.getEmailAddress();
            }

            if (mLabel.length() > MAX_LABEL_LENGTH) {
                mLabel = mLabel.substring(0, MAX_LABEL_LENGTH) + "...";
            }
        }

        public View getView() {
            if (mView == null) {
                mView = (RelativeLayout) inflate(getContext(), R.layout.chips_view, null);
                mView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (32 * mDensity)));
                mAvatarView = (ImageView) mView.findViewById(R.id.ri_ch_avatar);
                mIconWrapper = mView.findViewById(R.id.rl_ch_avatar);
                mTextView = (TextView) mView.findViewById(R.id.tv_ch_name);
                mPersonIcon = (ImageView) mView.findViewById(R.id.iv_ch_person);
                mCloseIcon = (ImageView) mView.findViewById(R.id.iv_ch_close);

                mErrorIcon = (ImageView) mView.findViewById(R.id.iv_ch_error);

                // set inital res & attrs
                mView.setBackgroundResource(mChipsBgRes);
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.getBackground().setColorFilter(mChipsBgColor, PorterDuff.Mode.SRC_ATOP);
                    }
                });
                mIconWrapper.setBackgroundResource(R.drawable.circle);
                mTextView.setTextColor(mChipsTextColor);

                // set icon resources
                mPersonIcon.setBackgroundResource(mChipsPlaceholderResId);
                mCloseIcon.setBackgroundResource(mChipsDeleteResId);


                mView.setOnClickListener(this);
                mIconWrapper.setOnClickListener(this);
            }
            updateViews();
            return mView;
        }

        private void updateViews() {
            mTextView.setText(mLabel);
            if (mPhotoUri != null) {
                Picasso.with(getContext())
                        .load(mPhotoUri)
                        .noPlaceholder()
                        .into(mAvatarView, new Callback() {
                            @Override
                            public void onSuccess() {
                                mPersonIcon.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
            if (isSelected()) {
                if (mChipsValidator != null && !mChipsValidator.isValid(mContact)) {
                    // not valid & show error
                    mView.getBackground().setColorFilter(mChipsBgColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                    mTextView.setTextColor(mChipsTextColorErrorClicked);
                    mIconWrapper.getBackground().setColorFilter(mChipsColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                    mErrorIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                } else {
                    mView.getBackground().setColorFilter(mChipsBgColorClicked, PorterDuff.Mode.SRC_ATOP);
                    mTextView.setTextColor(mChipsTextColorClicked);
                    mIconWrapper.getBackground().setColorFilter(mChipsColorClicked, PorterDuff.Mode.SRC_ATOP);
                }
                mPersonIcon.animate().alpha(0.0f).setDuration(200).start();
                mAvatarView.animate().alpha(0.0f).setDuration(200).start();
                mCloseIcon.animate().alpha(1f).setDuration(200).setStartDelay(100).start();

            } else {
                if (mChipsValidator != null && !mChipsValidator.isValid(mContact)) {
                    // not valid & show error
                    mErrorIcon.setVisibility(View.VISIBLE);
                    mErrorIcon.setColorFilter(null);
                } else {
                    mErrorIcon.setVisibility(View.GONE);
                }
                mView.getBackground().setColorFilter(mChipsBgColor, PorterDuff.Mode.SRC_ATOP);
                mTextView.setTextColor(mChipsTextColor);
                mIconWrapper.getBackground().setColorFilter(mChipsColor, PorterDuff.Mode.SRC_ATOP);

                mPersonIcon.animate().alpha(0.3f).setDuration(200).setStartDelay(100).start();
                mAvatarView.animate().alpha(1f).setDuration(200).setStartDelay(100).start();
                mCloseIcon.animate().alpha(0.0f).setDuration(200).start();
            }
        }

        @Override
        public void onClick(View v) {
            mEditText.clearFocus();
            if (v.getId() == mView.getId()) {
                onChipInteraction(this, true);
            } else {
                onChipInteraction(this, false);
            }
        }

        public boolean isSelected() {
            return mIsSelected;
        }

        public void setSelected(boolean isSelected) {
            if (mIsIndelible) {
                return;
            }
            this.mIsSelected = isSelected;
        }

        public Contact getContact() {
            return mContact;
        }

        @Override
        public boolean equals(Object o) {
            if (mContact != null && o instanceof Contact) {
                return mContact.equals(o);
            }
            return super.equals(o);
        }
    }

    public interface ChipsListener {
        void onChipAdded(Chip chip);

        void onChipDeleted(Chip chip);

        void onTextChanged(CharSequence text);
    }

    public static abstract class ChipValidator {
        public abstract boolean isValid(Contact contact);
    }
}
