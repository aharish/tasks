/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.gtasks;

import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.dao.StoreObjectDao;
import com.todoroo.astrid.dao.StoreObjectDao.StoreObjectCriteria;
import com.todoroo.astrid.data.StoreObject;

import java.util.HashSet;
import java.util.List;

public class GtasksListService {

    public static final String LIST_NOT_FOUND = null;
    public static final StoreObject LIST_NOT_FOUND_OBJECT = null;

    @Autowired
    private StoreObjectDao storeObjectDao;

    private StoreObject[] lists = null;

    public GtasksListService() {
        DependencyInjectionService.getInstance().inject(this);
    }

    private void readLists() {
        if(lists != null) {
            return;
        }

        TodorooCursor<StoreObject> cursor = storeObjectDao.query(Query.select(StoreObject.PROPERTIES).
                where(StoreObjectCriteria.byType(GtasksList.TYPE)));
        try {
            lists = new StoreObject[cursor.getCount()];
            for(int i = 0; i < lists.length; i++) {
                cursor.moveToNext();
                StoreObject dashboard = new StoreObject(cursor);
                lists[i] = dashboard;
            }
        } finally {
            cursor.close();
        }
    }

    public StoreObject[] getLists() {
        readLists();
        return lists;
    }

    /**
     * Get list name
     * @return NOT_FOUND if no list by this id exists, otherwise list name
     */
    public String getListName(String listId) {
        StoreObject list = getList(listId);
        if(list != LIST_NOT_FOUND_OBJECT) {
            return list.getValue(GtasksList.NAME);
        }
        return LIST_NOT_FOUND;
    }

    /**
     * Reads in remote list information and updates local list objects.
     *
     * @param remoteLists remote information about your lists
     */
    public synchronized void updateLists(TaskLists remoteLists) {
        readLists();

        HashSet<Long> previousLists = new HashSet<Long>(lists.length);
        for(StoreObject list : lists) {
            previousLists.add(list.getId());
        }

        List<TaskList> items = remoteLists.getItems();
        StoreObject[] newLists = new StoreObject[items.size()];
        for(int i = 0; i < items.size(); i++) {
            com.google.api.services.tasks.model.TaskList remote = items.get(i);

            String id = remote.getId();
            StoreObject local = null;
            for(StoreObject list : lists) {
                if(list.getValue(GtasksList.REMOTE_ID).equals(id)) {
                    local = list;
                    break;
                }
            }

            if(local == null) {
                local = new StoreObject();
            }

            local.setValue(StoreObject.TYPE, GtasksList.TYPE);
            local.setValue(GtasksList.REMOTE_ID, id);
            local.setValue(GtasksList.NAME, remote.getTitle());
            local.setValue(GtasksList.ORDER, i);
            storeObjectDao.persist(local);
            previousLists.remove(local.getId());
            newLists[i] = local;
        }
        lists = newLists;

        // check for lists that aren't on remote server
        for(Long listId : previousLists) {
            storeObjectDao.delete(listId);
        }
    }

    public StoreObject addNewList(com.google.api.services.tasks.model.TaskList newList) {
        readLists();

        if (lists != null) {
            for (StoreObject list : lists) {
                if (list.getValue(GtasksList.REMOTE_ID).equals(newList.getId())) //Sanity check--make sure it's actually a new list
                {
                    return null;
                }
            }
        }
        StoreObject local = new StoreObject();

        local.setValue(StoreObject.TYPE, GtasksList.TYPE);
        local.setValue(GtasksList.REMOTE_ID, newList.getId());
        local.setValue(GtasksList.NAME, newList.getTitle());

        int order = lists == null ? 0 : lists.length;
        local.setValue(GtasksList.ORDER, order);

        storeObjectDao.persist(local);

        clearListCache();
        return local;
    }

    private void clearListCache() {
        lists = null;
    }

    public StoreObject getList(String listId) {
        readLists();
        for(StoreObject list : lists) {
            if (list != null && list.getValue(GtasksList.REMOTE_ID).equals(listId)) {
                return list;
            }
        }
        return LIST_NOT_FOUND_OBJECT;
    }

}