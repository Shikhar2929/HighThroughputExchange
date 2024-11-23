# HighThroughputExchange

---

<h3>API Design</h3>

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
        <td>public</td>
        <td>Information on how each team is doing.</td>
        <td>N/A - just a simple HTTP GET</td>
        <td>JSON of team performance in sorted order.</td>
    </tr>
    <tr>
        <td>Trends</td>
        <td>/trends</td>
        <td>public</td>
        <td>Gets key timestamps to visualize historical trends of the performance of a stock.</td>
        <td>N/A - just a simple HTTP GET</td>
        <td>JSON representing historical stock data</td>
    </tr>
    <tr>
        <td>Admin Dashboard</td>
        <td>/admin</td>
        <td>admin</td>
        <td>Allows admin to view all current teams + all dummy bots registered on the platform, as well as all active sessions trading.</td>
        <td>{"adminUsername": string, "adminPassword": string}</td>
        <td>{"auth": boolean}</td>
    </tr>
    <tr>
        <td>Add Team</td>
        <td>/add_team</td>
        <td>admin</td>
        <td>Allows the admin to submit a new team's registration information.</td>
        <td>{"adminUsername": string, "adminPassword": string, "username": string, "name": string}</td>
        <td>{"auth": boolean, "success": boolean, "message": string}</td>
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
        <td>Order</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
</table>