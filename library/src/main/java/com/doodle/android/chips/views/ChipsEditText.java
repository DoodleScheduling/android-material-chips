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

package com.doodle.android.chips.views;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class ChipsEditText extends AppCompatEditText {

    private InputConnectionWrapperInterface mInputConnectionWrapperInterface;
    private OnKeyboardCloseListener mOnKeyboardCloseListener;

    public ChipsEditText(Context context, InputConnectionWrapperInterface inputConnectionWrapperInterface) {
        super(context);
        this.mInputConnectionWrapperInterface = inputConnectionWrapperInterface;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (mInputConnectionWrapperInterface != null) {
            return mInputConnectionWrapperInterface.getInputConnection(super.onCreateInputConnection(outAttrs));
        }

        return super.onCreateInputConnection(outAttrs);
    }

    public interface InputConnectionWrapperInterface {
        InputConnection getInputConnection(InputConnection target);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mOnKeyboardCloseListener != null)
                return mOnKeyboardCloseListener.onKeyboardClose();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnKeyboardCloseListener(OnKeyboardCloseListener mOnKeyboardClose) {
        this.mOnKeyboardCloseListener = mOnKeyboardClose;
    }

    public interface OnKeyboardCloseListener {
        boolean onKeyboardClose();
    }
}
