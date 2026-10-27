/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.systemui.R; // use o mesmo import do KeyguardPINView.java

/**
 * View da Tela Soma. Responsável apenas pela apresentação;
 * toda a lógica fica em {@link KeyguardMathViewController}.
 */
public class KeyguardMathView extends KeyguardInputView {

    private TextView mChallengeView;
    private EditText mAnswerView;
    private TextView mErrorView;
    private Button mConfirmButton;

    public KeyguardMathView(Context context) {
        this(context, null);
    }

    public KeyguardMathView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChallengeView = findViewById(R.id.math_challenge);
        mAnswerView = findViewById(R.id.math_answer);
        mErrorView = findViewById(R.id.math_error);
        mConfirmButton = findViewById(R.id.math_confirm);
    }

    /** Mostra um novo desafio e limpa a resposta anterior. */
    void showChallenge(int first, int second) {
        mChallengeView.setText(
                getContext().getString(R.string.keyguard_math_challenge, first, second));
        mAnswerView.setText("");
    }

    /** Mostra ou esconde a mensagem de resposta errada. */
    void setErrorVisible(boolean visible) {
        mErrorView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    String getAnswerText() {
        return mAnswerView.getText().toString().trim();
    }

    EditText getAnswerView() {
        return mAnswerView;
    }

    Button getConfirmButton() {
        return mConfirmButton;
    }

    @Override
    public CharSequence getTitle() {
        return getContext().getString(R.string.keyguard_math_title);
    }
}