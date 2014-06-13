/*
 * Copyright 2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.elsevier.kinesis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * This is the data model for the objects being sent through the Kinesis streams in the samples
 * 
 */
public class KinesisMessageModel {

    public String docid;
    public String key;
    public String action;
    public String epoch;
    public String timestamp;


    /**
     * Default constructor for Jackson JSON mapper - uses bean pattern
     */
    public KinesisMessageModel(){
        
    }
    
    /**
     * 
     * @param docid
     *            Sample String data field
     * @param key
     *            Sample String data field
     * @param action
     *            Sample String data field
     * @param epoch
     *            Sample String data field
     * @param timestamp
     *            Sample String data field
     */
    public KinesisMessageModel(String docid, String key, String action, String epoch, String timestamp) {
        this.docid = docid;
        this.key = key;
        this.action = action;
        this.epoch = epoch;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }

    /**
     * Getter for docid
     * 
     * @return docid
     */
    public String getDocid() {
        return docid;
    }

    /**
     * Setter for docid
     * 
     * @param docid
     *            Value for docid
     */
    public void setDocid(String docid) {
        this.docid = docid;
    }

    /**
     * Getter for key
     * 
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Setter for key
     * 
     * @param username
     *            Value for key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Getter for action
     * 
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * Setter for action
     * 
     * @param action
     *            Value for action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Getter for epoch
     * 
     * @return epoch
     */
    public String getEpoch() {
        return epoch;
    }

    /**
     * Setter for epoch
     * 
     * @param epoch
     *            Value for epoch
     */
    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    /**
     * Getter for timestamp
     * 
     * @return timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Setter for timestamp
     * 
     * @param timestamp
     *            Value for timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((docid == null) ? 0 : docid.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((epoch == null) ? 0 : epoch.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof KinesisMessageModel)) {
            return false;
        }
        KinesisMessageModel other = (KinesisMessageModel) obj;
        if (docid == null) {
            if (other.docid != null) {
                return false;
            }
        } else if (!docid.equals(other.docid)) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (action == null) {
            if (other.action != null) {
                return false;
            }
        } else if (!action.equals(other.action)) {
            return false;
        }
        if (epoch == null) {
            if (other.epoch != null) {
                return false;
            }
        } else if (!epoch.equals(other.epoch)) {
            return false;
        }
        if (timestamp == null) {
            if (other.timestamp != null) {
                return false;
            }
        } else if (!timestamp.equals(other.timestamp)) {
            return false;
        }        
        return true;
    }
}
