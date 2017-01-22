/*
 * Copyright (c) 2017 Nrupesh Patel
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.nrupeshpatel.cleancity.adapter;

public class Complaint {

    private String id, status, detail, date, address, image;
    private boolean starred;

    public Complaint() {

    }

    public Complaint(String id, String status, String detail, String date, String address, boolean starred, String image) {
        this.id = id;
        this.status = status;
        this.detail = detail;
        this.date = date;
        this.address = address;
        this.starred = starred;
        this.image = image;
    }

    public String getId() {
        return this.id;
    }

    String getStatus() {
        return this.status;
    }

    String getDetail() {
        return this.detail;
    }

    String getDate() {
        return this.date;
    }

    String getAddress() {
        return this.address;
    }

    boolean getStarred() {
        return this.starred;
    }

    String getImage() {
        return this.image;
    }

}