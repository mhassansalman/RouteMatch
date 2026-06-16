const API_BASE = "http://localhost:8081/api";

window.onload = function () {
    loadLocations();
};

async function loadLocations() {
    try {
        const response = await fetch(`${API_BASE}/locations`);
        const locations = await response.json();

        const source = document.getElementById("source");
        const destination = document.getElementById("destination");

        locations.forEach(location => {
            source.add(new Option(location.name, location.id));
            destination.add(new Option(location.name, location.id));
        });

    } catch (error) {
        alert("Could not load locations. Make sure backend is running.");
    }
}

// NEW — trie autocomplete
async function onTrieInput(field) {
    const input = document.getElementById(field + "Input");
    const dropdown = document.getElementById(field + "Dropdown");
    const select = document.getElementById(field);

    const prefix = input.value.trim();

    if (prefix.length === 0) {
        dropdown.innerHTML = "";
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/locations/search?prefix=${encodeURIComponent(prefix)}`);
        const locations = await response.json();

        dropdown.innerHTML = "";
        locations.forEach(loc => {
            const li = document.createElement("li");
            li.textContent = loc.name;
            li.onclick = () => {
                input.value = loc.name;
                select.value = loc.id;  // syncs with the <select> so findMatches() works unchanged
                dropdown.innerHTML = "";
            };
            dropdown.appendChild(li);
        });
    } catch (error) {
        console.error("Trie search failed:", error);
    }
}

async function findMatches() {
    const source = document.getElementById("source");
    const destination = document.getElementById("destination");

    if (source.value === "" || destination.value === "") {
        alert("Please select source and destination.");
        return;
    }

    if (source.value === destination.value) {
        alert("Source and destination cannot be same.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/match`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                sourceId: Number(source.value),
                destinationId: Number(destination.value)
            })
        });

        const data = await response.json();
        showResults(data);

    } catch (error) {
        alert("Could not find matches. Make sure backend is running.");
    }
}

function showResults(data) {
    document.getElementById("routePath").textContent = data.routePath;
    document.getElementById("totalTime").textContent = data.totalTime;
    document.getElementById("matchCount").textContent = data.matchCount;
    document.getElementById("routeInfo").style.display = "block";

    const table = document.getElementById("resultsTable");
    const body = document.getElementById("resultsBody");

    body.innerHTML = "";

    data.matches.forEach(match => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${match.rank}</td>
            <td>${match.passengerName}</td>
            <td>${match.pickup}</td>
            <td>${match.dropoff}</td>
            <td>${match.detourCost} min</td>
            <td>${match.score}%</td>
        `;
        body.appendChild(row);
    });

    table.style.display = "table";
}

function clearForm() {
    document.getElementById("source").selectedIndex = 0;
    document.getElementById("destination").selectedIndex = 0;
    document.getElementById("sourceInput").value = "";
    document.getElementById("destinationInput").value = "";
    document.getElementById("sourceDropdown").innerHTML = "";
    document.getElementById("destinationDropdown").innerHTML = "";
    document.getElementById("resultsBody").innerHTML = "";
    document.getElementById("resultsTable").style.display = "none";
    document.getElementById("routeInfo").style.display = "none";
}