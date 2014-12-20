/**
 * Copyright 2005-2013 hdiv.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hdiv.session;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * It is composed by a data structure limited by a maximum size (maxSize). Map data structure is composed by elements of
 * type IPage (all the possible requests generated in the request processing).
 * 
 * @author Roberto Velasco
 */
public class StateCache implements IStateCache {

	private static final int DEFAULT_MAX_SIZE = 5;

	/**
	 * Commons Logging instance.
	 */
	private static final Log log = LogFactory.getLog(StateCache.class);

	/**
	 * Universal version identifier. Deserialization uses this number to ensure that a loaded class corresponds exactly
	 * to a serialized object.
	 */
	private static final long serialVersionUID = -386843742684433849L;

	/**
	 * Buffer size
	 */
	private int maxSize = DEFAULT_MAX_SIZE;

	/**
	 * page's ids map
	 */
	private List<Integer> pageIds = new ArrayList<Integer>();

	/**
	 * Adds a new page identifier to the map <code>pageIds</code>.
	 * 
	 * @return If the map <code>pageIds</code> has reached its maximum size <code>maxSize</code>, the oldest page
	 *         identifier is deleted. Otherwise, null will be returned.
	 */
	public synchronized List<Integer> addPage(String key, String currentPageId) {

	  // TODO, should already be an integer
    Integer iKey = Integer.valueOf(key);
    Integer iCurrentKey = Integer.valueOf(currentPageId);
	  
		if (this.pageIds.contains(iKey)) {
			// Page id already exist in session
			return null;

		} else {
		  
			List<Integer> removedKeys = this.cleanBuffer(iCurrentKey);
			
			this.pageIds.add(iKey);

			if (log.isDebugEnabled()) {
				log.debug("Page with [" + key + "] added to the cache.");
			}

			return removedKeys;
		}
	}

	/**
	 * If the map <code>pageIds</code> has reached its maximum size <code>maxSize</code>, on page is deleted.
	 * If current page is the last one, the oldest key is removed, otherwise any newer page is removed
	 * 
	 * @return Oldest page identifier in the map <code>pageIds</code>. Null in otherwise.
	 */
  private List<Integer> cleanBuffer(Integer currentPageId) {

    ArrayList<Integer> removed = new ArrayList<Integer>();

    if (currentPageId > 0) {

      for (int i = this.pageIds.size() - 1; i >= 0; i--) {
        if (this.pageIds.get(i) > currentPageId) {
          removed.add(this.pageIds.remove(i));
        }
      }
    }
    
    if (this.pageIds.size() >= this.maxSize) {
      removed.add(this.pageIds.remove(0));
    }
    
    if (log.isDebugEnabled()) {
        log.debug("Full Cache, deleted page with id [" + removed + "].");
    }
    return removed;
	}

	public String toString() {

		StringBuffer result = new StringBuffer();
		result.append("[");
		for (Integer pageId : pageIds) {
			result.append(" " + pageId);
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * @return Returns the maxSize.
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * @param maxSize
	 *            The maxSize to set.
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * @return the pageIds
	 */
	public List<String> getPageIds() {
		return null; //TODO
	}

}