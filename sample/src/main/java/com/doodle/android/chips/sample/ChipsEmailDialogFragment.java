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

package com.doodle.android.chips.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.doodle.android.chips.R;
import com.doodle.android.chips.util.Common;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ChipsEmailDialogFragment extends DialogFragment {

    public static final String EXTRA_STRING_TEXT = "extra.string.text";

    public static final String EXTRA_STRING_TITLE = "extra.string.title";
    public static final String EXTRA_STRING_PLACEHOLDER = "extra.string.placeholder";
    public static final String EXTRA_STRING_CONFIRM = "extra.string.confirm";
    public static final String EXTRA_STRING_CANCEL = "extra.string.cancel";
    public static final String EXTRA_STRING_ERROR_MSG = "extra.string.error.msg";

    private String mErrorMsg;

    private MaterialEditText mEditText;
    private Button mConfirm;
    private Button mCancel;

    private EmailListener mEmailListener;

    private String mInitialText = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.dialog_chips_email, null);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setView(view)
                .create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        setCancelable(true);

        mEditText = (MaterialEditText) view.findViewById(R.id.et_ch_email);
        mConfirm = (Button) view.findViewById(R.id.bu_ch_confirm);
        mCancel = (Button) view.findViewById(R.id.bu_ch_cancel);

        TextView titleView = (TextView) view.findViewById(R.id.tv_ch_title);

        mErrorMsg = getString(R.string.please_enter_a_valid_email_address);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_STRING_TEXT)) {
                mInitialText = bundle.getString(EXTRA_STRING_TEXT);
                mEditText.setText(mInitialText);
            }
            if (bundle.containsKey(EXTRA_STRING_TITLE))
                titleView.setText(bundle.getString(EXTRA_STRING_TITLE));
            if (bundle.containsKey(EXTRA_STRING_PLACEHOLDER)) {
                mEditText.setHint(bundle.getString(EXTRA_STRING_PLACEHOLDER));
                mEditText.setFloatingLabelText(bundle.getString(EXTRA_STRING_PLACEHOLDER));
            }
            if (bundle.containsKey(EXTRA_STRING_CONFIRM))
                mConfirm.setText(bundle.getString(EXTRA_STRING_CONFIRM));
            if (bundle.containsKey(EXTRA_STRING_CANCEL))
                mCancel.setText(bundle.getString(EXTRA_STRING_CANCEL));
            if (bundle.containsKey(EXTRA_STRING_ERROR_MSG))
                mErrorMsg = bundle.getString(EXTRA_STRING_ERROR_MSG);
        }

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positiveClick();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                negativeClick();
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                positiveClick();
                return true;
            }
        });

        return dialog;
    }

    private void positiveClick() {
        String text = mEditText.getText().toString();
        if (text.length() > 0 && Common.isValidEmail(text.trim())) {
            if (mEmailListener != null) {
                mEmailListener.onDialogEmailEntered(text, mInitialText);
            }
            dismiss();
        } else {
            mEditText.setError(mErrorMsg);
        }
    }

    private void negativeClick() {
        dismiss();
    }

    public void setEmailListener(EmailListener emailListener) {
        this.mEmailListener = emailListener;
    }

    public interface EmailListener {
        void onDialogEmailEntered(String text, String displayName);
    }
}