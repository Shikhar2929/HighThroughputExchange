# HighThroughputExchange

---

<h2>API Design</h2>

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
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>Leaderboard</td>
        <td>/leaderboard</td>
        <td>public</td>
        <td>Information on how each team is doing.</td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>Login</td>
        <td>/login</td>
        <td>public</td>
        <td>Allows teams/admin to login to see their dashboards.</td>
        <td>{"username": string, "password": string}</td>
        <td>{"success": boolean, "sessionToken": string}</td>
    </tr>
    <tr>
        <td>Admin Dashboard</td>
        <td>/admin</td>
        <td>admin</td>
        <td>Allows admin to view all current teams + all dummy bots registered on the platform, as well as all active sessions trading.</td>
        <td>{"sessionToken": string}</td>
        <td>{"auth": boolean}</td>
    </tr>
    <tr>
        <td>Add Team</td>
        <td>/add_team</td>
        <td>admin</td>
        <td>Allows the admin to submit a new team's registration information.</td>
        <td>{"sessionToken": string}</td>
        <td>{"auth": boolean}</td>
    </tr>
    <tr>
        <td>Buildup</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>Teardown</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
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