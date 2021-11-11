class TimeSync {

    constructor(url) {
        let socket = new WebSocket(url)
        socket.binaryType = "arraybuffer"

        socket.onmessage = (message) => {
            console.log("Time synchronized")
            const view = new DataView(message.data)
            this.lastServerTime = Number(view.getBigUint64(0))
            this.lastClientTime = new Date().getUTCMilliseconds();
        }
    }

    /**
     * Returns current server time
     */
    getServerTime() {
        return this.lastServerTime + (new Date().getUTCMilliseconds() - this.lastClientTime)
    }

    /**
     * Returns current local time based on server time and local time zone
     */
    getLocalTime() {
        const MILLIS_PER_MINUTE = 60 * 1000;
        return this.getServerTime() - new Date().getTimezoneOffset() * MILLIS_PER_MINUTE;
    }

}

export default TimeSync;