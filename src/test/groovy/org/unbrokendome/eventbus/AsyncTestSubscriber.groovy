package org.unbrokendome.eventbus


class AsyncTestSubscriber {

    @Subscribe(async = true)
    void handleTestEvent(AsyncTestEvent event) { }
}
