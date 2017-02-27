/*
 * Copyright (C) 2016 Doodle.
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

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.doodle.android.chips.ChipsView;
import com.doodle.android.chips.model.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mContacts;
    private ContactsAdapter mAdapter;
    private ChipsView mChipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContacts = (RecyclerView) findViewById(R.id.rv_contacts);
        mContacts.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mAdapter = new ContactsAdapter();
        mContacts.setAdapter(mAdapter);

        mChipsView = (ChipsView) findViewById(R.id.cv_contacts);

        mChipsView.setTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/FiraSans-Medium.ttf"));
        // mChipsView.useInitials(14, Typeface.createFromAsset(this.getAssets(), "fonts/FiraSans-Medium.ttf"), Color.RED);

        // change EditText config
        mChipsView.getEditText().setCursorVisible(true);

        mChipsView.setChipsValidator(new ChipsView.ChipValidator() {
            @Override
            public boolean isValid(Contact contact) {
                if (contact.getDisplayName().equals("asd@qwe.de")) {
                    return false;
                }
                return true;
            }
        });

        mChipsView.setChipsListener(new ChipsView.ChipsListener() {
            @Override
            public void onChipAdded(ChipsView.Chip chip) {
                for (ChipsView.Chip chipItem : mChipsView.getChips()) {
                    Log.d("ChipList", "chip: " + chipItem.toString());
                }
            }

            @Override
            public void onChipDeleted(ChipsView.Chip chip) {

            }

            @Override
            public void onTextChanged(CharSequence text) {
                mAdapter.filterItems(text);
            }

            @Override
            public void onInputNotValid(String text) {

                try {
                    FragmentManager fragmentManager = ((FragmentActivity) MainActivity.this).getSupportFragmentManager();

                    Bundle bundle = new Bundle();
                    bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_TEXT, text);
                    bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_TITLE, "Title");
                    bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_PLACEHOLDER, "ChipsDialogPlaceholder");
                    bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_CONFIRM, "ChipsDialogConfirm");
                    bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_CANCEL, "ChipsDialogCancel");
                    bundle.putString(ChipsEmailDialogFragment.EXTRA_STRING_ERROR_MSG, "ChipsDialogErrorMsg");

                    ChipsEmailDialogFragment chipsEmailDialogFragment = new ChipsEmailDialogFragment();
                    chipsEmailDialogFragment.setArguments(bundle);
                    chipsEmailDialogFragment.setEmailListener(new ChipsEmailDialogFragment.EmailListener() {
                        @Override
                        public void onDialogEmailEntered(String text, String displayName) {
                            mChipsView.addChip(displayName, null, new Contact(null, null, displayName, text, null), false);
                        }
                    });
                    chipsEmailDialogFragment.show(fragmentManager, ChipsEmailDialogFragment.class.getSimpleName());
                } catch (ClassCastException e) {
                    Log.e("CHIPS", "Error ClassCast", e);
                }
            }
        });
    }

    public class ContactsAdapter extends RecyclerView.Adapter<CheckableContactViewHolder> {

        private String[] data = new String[]{
                "john@doe.com",
                "at@doodle.com",
                "asd@qwe.de",
                "verylongaddress@verylongserver.com",
                "thisIsMyEmail@address.com",
                "test@testeration.de",
                "short@short.com"
        };

        private List<String> filteredList = new ArrayList<>();

        public ContactsAdapter() {
            Collections.addAll(filteredList, data);
        }

        @Override
        public CheckableContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_checkable_contact, parent, false);
            return new CheckableContactViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CheckableContactViewHolder holder, int position) {
            holder.name.setText(filteredList.get(position));
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public void filterItems(CharSequence text) {
            filteredList.clear();
            if (TextUtils.isEmpty(text)) {
                Collections.addAll(filteredList, data);
            } else {
                for (String s : data) {
                    if (s.contains(text)) {
                        filteredList.add(s);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return Math.abs(filteredList.get(position).hashCode());
        }
    }

    public class CheckableContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView name;
        public final CheckBox selection;

        public CheckableContactViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_contact_name);
            selection = (CheckBox) itemView.findViewById(R.id.cb_contact_selection);
            selection.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selection.performClick();
                }
            });
        }

        @Override
        public void onClick(View v) {
            String email = name.getText().toString();
            Uri imgUrl = Math.random() > .7d ? null : Uri.parse("https://robohash.org/" + Math.abs(email.hashCode()));
            Contact contact = new Contact(null, null, null, email, imgUrl);

            if (selection.isChecked()) {
                boolean indelibe = Math.random() > 0.2f;
                mChipsView.addChip(email, imgUrl, contact, indelibe);
            } else {
                mChipsView.removeChipBy(contact);
            }
        }
    }
}
