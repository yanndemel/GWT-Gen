package com.google.gwt.user.client.ui;



/**
   * A callback that is used to set the position of a {@link ResizePopupPanel} right
   * before it is shown.
   */
  public interface PositionCallback {

    /**
     * Provides the opportunity to set the position of the PopupPanel right
     * before the PopupPanel is shown. The offsetWidth and offsetHeight values
     * of the PopupPanel are made available to allow for positioning based on
     * its size.
     *
     * @param offsetWidth the offsetWidth of the PopupPanel
     * @param offsetHeight the offsetHeight of the PopupPanel
     * @see ResizePopupPanel#setPopupPositionAndShow(PositionCallback)
     */
    void setPosition(int offsetWidth, int offsetHeight);
  }