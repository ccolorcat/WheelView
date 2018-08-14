/*
 * Copyright 2018 cxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.colorcat.wheelview.sample;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: cxx
 * Date: 2018-08-13
 * GitHub: https://github.com/ccolorcat
 */
public class RegionLoader {
    private final Context mContext;
    private final List<Province> mProvinces = new ArrayList<>(31);

    public RegionLoader(Context context) {
        mContext = context;
    }

    public List<Province> get() {
        return Collections.unmodifiableList(mProvinces);
    }

    public void load(final OnLoadedListener listener) {
        if (!mProvinces.isEmpty()) {
            listener.onLoaded(get());
            return;
        }
        new AsyncLoader(new OnLoadedListener() {
            @Override
            public void onLoaded(List<Province> provinces) {
                mProvinces.clear();
                mProvinces.addAll(provinces);
                listener.onLoaded(provinces);
            }
        }).execute(mContext);
    }


    private static class AsyncLoader extends AsyncTask<Context, Void, List<Province>> {
        private final OnLoadedListener mListener;

        private AsyncLoader(OnLoadedListener listener) {
            mListener = listener;
        }

        @Override
        protected List<Province> doInBackground(Context... contexts) {
            Reader reader = new InputStreamReader(contexts[0].getResources().openRawResource(R.raw.region));
            TypeToken<List<Province>> token = new TypeToken<List<Province>>() {};
            return new GsonBuilder().create().fromJson(reader, token.getType());
        }

        @Override
        protected void onPostExecute(List<Province> provinces) {
            super.onPostExecute(provinces);
            mListener.onLoaded(provinces);
        }
    }


    public interface OnLoadedListener {
        void onLoaded(List<Province> provinces);
    }
}
