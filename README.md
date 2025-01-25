# HighThroughputExchange

---

<h3>API Design</h3>

[//]: # (Todo: stopping mechanism; action endpoint; show auction results; frontend orderbook; individual order cancelation)

{
    "game_name": "X Cards",
    "rounds": 10,
    ""
}

<table>
    <tr>
        <th>Name</th>
        <th>Route</th>
        <th>Visibility</th>
        <th>Description</th>
        <th>Input</th>
        <th>Output</th>
    </tr>
    <tr>
        <td>Home</td>
        <td>/</td>
        <td>public</td>
        <td>General landing homepage for competition.</td>
        <td>N/A - just a simple HTTP GET</td>
        <td>N/A - just a raw, static frontend</td>
    </tr>
    <tr>
        <td>Leaderboard</td>
        <td>/leaderboard</td>
        <td>admin</td>
        <td>Displays team ranking by PnL.</td>
        <td>N/A - just a simple HTTP GET</td>
        <td>N/A - just a raw, static frontend</td>
    </tr>
    <tr>
        <td>Add User</td>
        <td>/add_user</td>
        <td>admin</td>
        <td>Allows the admin to submit a new team's registration information.</td>
        <td>{"adminUsername": string, "adminPassword": string, "username": string, "name": string, "email": string}</td>
        <td>{"auth": boolean, "success": boolean, "message": string}</td>
    </tr>
    <tr>
        <td>Clear Round</td>
        <td>/clear_round</td>
        <td>admin</td>
        <td>Clears everything except for PNL.</td>
        <td>{"adminUsername": string, "adminPassword": string, "username": string, "name": string, "email": string}</td>
        <td>{"auth": boolean, "success": boolean, "message": string}</td>
    </tr>
    <tr>
        <td>Shutdown</td>
        <td>/shutdown</td>
        <td>admin</td>
        <td>Saves all the database states in a JSON file.</td>
        <td>{"adminUsername": string, "adminPassword": string}</td>
        <td>Success/Failure</td>
    </tr>
    <tr>
        <td>Buildup</td>
        <td>/buildup</td>
        <td>private</td>
        <td>Authenticates a team, and then issues a sessionToken for the team to use to make trades and subscribe to the websocket.</td>
        <td>{"username": string, "apiKey": string}</td>
        <td>{"auth": boolean, "success": boolean, "message": string}</td>
    </tr>
    <tr>
        <td>Teardown</td>
        <td>/teardown</td>
        <td>private</td>
        <td>Closes the sessionToken of the team, terminating their ability to subscribe to the websocket.</td>
        <td>{"username": string, "sessionToken": string}</td>
        <td>{"auth": string, "success": string}</td>
    </tr>
    <tr>
        <td>Limit Order</td>
        <td>/limit_order</td>
        <td>private</td>
        <td>Places a limit order for team.</td>
        <td>{"username": string, "sessionToken": string, "ticker": string, "volume": float, "price": float, "isBid": boolean}</td>
        <td>Success/Fail</td>
    </tr>
    <tr>
        <td>Market Order</td>
        <td>/market_order</td>
        <td>private</td>
        <td>Places a market order for team.</td>
        <td>{"username": string, "sessionToken": string, "ticker": string, "volume": float, "isBid": boolean}</td>
        <td>Success/Fail</td>
    </tr>
    <tr>
        <td>Remove All</td>
        <td>/remove_all</td>
        <td>private</td>
        <td>Removes all active orders of team.</td>
        <td>{"username": string, "sessionToken": string}</td>
        <td>Success/Fail</td>
    </tr>
    <tr>
        <td>Auction</td>
        <td>/auction</td>
        <td>private</td>
        <td>Facilitates the auction.</td>
        <td>{"username": string, "sessionToken": string, "price": float}</td>
        <td>Success/Fail</td>
    </tr>
</table>



[//]: # (<tr>)

[//]: # (        <td>Trends</td>)

[//]: # (        <td>/trends</td>)

[//]: # (        <td>public</td>)

[//]: # (        <td>Gets key timestamps to visualize historical trends of the performance of a stock.</td>)

[//]: # (        <td>N/A - just a simple HTTP GET</td>)

[//]: # (        <td>JSON representing historical stock data</td>)

[//]: # (    </tr>)

[//]: # (<tr>)

[//]: # (        <td>Leaderboard</td>)

[//]: # (        <td>/leaderboard</td>)

[//]: # (        <td>public</td>)

[//]: # (        <td>Information on how each team is doing.</td>)

[//]: # (        <td>N/A - just a simple HTTP GET</td>)

[//]: # (        <td>JSON of team performance in sorted order.</td>)

[//]: # (    </tr>)

[//]: # (    <tr>)

[//]: # (        <td>Admin Dashboard</td>)

[//]: # (        <td>/admin</td>)

[//]: # (        <td>admin</td>)

[//]: # (        <td>Allows admin to view all current teams + all dummy bots registered on the platform, as well as all active sessions trading.</td>)

[//]: # (        <td>{"adminUsername": string, "adminPassword": string}</td>)

[//]: # (        <td>{"auth": boolean}</td>)

[//]: # (    </tr>)