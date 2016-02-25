/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.jhu.cs.hinrg.dailyalert.android.adapters;

import edu.jhu.cs.hinrg.dailyalert.android.logic.HierarchyElement;
import edu.jhu.cs.hinrg.dailyalert.android.views.HierarchyElementView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class HierarchyListAdapter extends BaseAdapter {

    private Context mContext;
    private List<HierarchyElement> mItems = new ArrayList<HierarchyElement>();


    public HierarchyListAdapter(Context context) {
        mContext = context;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mItems.size();
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HierarchyElementView hev;
        if (convertView == null) {
            hev = new HierarchyElementView(mContext, mItems.get(position));
        } else {
            hev = (HierarchyElementView) convertView;
            hev.setPrimaryText(mItems.get(position).getPrimaryText());
            hev.setSecondaryText(mItems.get(position).getSecondaryText());
            hev.setIcon(mItems.get(position).getIcon());
            hev.setColor(mItems.get(position).getColor());
        }

        if (mItems.get(position).getSecondaryText() == null
                || mItems.get(position).getSecondaryText().equals("")) {
            hev.showSecondary(false);
        } else {
            hev.showSecondary(true);
        }
        return hev;

    }


    /**
     * Sets the list of items for this adapter to use.
     */
    public void setListItems(List<HierarchyElement> it) {
        mItems = it;
    }

}
