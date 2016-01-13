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

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

    private RecyclerView mRecyclerView;
    private MyAdapter mMyAdapter;
    private ChipsView mChipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mMyAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mMyAdapter);

        mChipsView = (ChipsView) findViewById(R.id.chipsView);

        mChipsView.setChipsListener(new ChipsView.ChipsListener() {
            @Override
            public void onChipAdded(ChipsView.Chip chip) {

            }

            @Override
            public void onChipDeleted(ChipsView.Chip chip) {

            }

            @Override
            public void onTextChanged(CharSequence text) {
                mMyAdapter.filterItems(text);
            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

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

        public MyAdapter() {
            Collections.addAll(filteredList, data);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(View.inflate(parent.getContext(), R.layout.item_my, null));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.textView.setText(filteredList.get(position));
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

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public final CheckBox checkBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_view);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String email = textView.getText().toString();
            Uri imgUrl = Math.random() > .7d ? null : Uri.parse("https://robohash.org/" + Math.abs(email.hashCode()));
            Contact contact = new Contact(null, null, null, email, imgUrl);

            if (checkBox.isChecked()) {
                mChipsView.removeChipBy(contact);
            } else {
                mChipsView.addChip(email, imgUrl, contact);
            }
            checkBox.toggle();
        }
    }
}
