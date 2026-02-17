const profileKey = "fitnessTrackerProfile";
const entriesKey = "fitnessTrackerEntries";

const heightInput = document.getElementById("height");
const weightInput = document.getElementById("weight");
const dateInput = document.getElementById("entryDate");
const stepsInput = document.getElementById("steps");
const waterInput = document.getElementById("water");
const sleepInput = document.getElementById("sleep");
const trainingInput = document.getElementById("trainingInput");
const trainingList = document.getElementById("trainingList");
const caloriesBurnt = document.getElementById("caloriesBurnt");
const savedEntries = document.getElementById("savedEntries");

const addTrainingBtn = document.getElementById("addTrainingBtn");
const saveBtn = document.getElementById("saveBtn");
const clearBtn = document.getElementById("clearBtn");

let trainingItems = [];

function todayISO() {
  const now = new Date();
  const tzOffsetMs = now.getTimezoneOffset() * 60 * 1000;
  return new Date(now.getTime() - tzOffsetMs).toISOString().slice(0, 10);
}

function loadProfile() {
  const profile = JSON.parse(localStorage.getItem(profileKey) || "{}");
  heightInput.value = profile.height ?? "";
  weightInput.value = profile.weight ?? "";
}

function saveProfile() {
  localStorage.setItem(
    profileKey,
    JSON.stringify({
      height: Number(heightInput.value) || "",
      weight: Number(weightInput.value) || "",
    })
  );
}

function estimateCalories() {
  const steps = Number(stepsInput.value) || 0;
  const weight = Number(weightInput.value) || 0;
  const heightCm = Number(heightInput.value) || 0;

  if (!steps || !weight || !heightCm) {
    caloriesBurnt.textContent = "0";
    return 0;
  }

  const strideMeters = heightCm * 0.414 / 100;
  const distanceKm = (steps * strideMeters) / 1000;
  const calories = distanceKm * weight * 1.036;
  caloriesBurnt.textContent = calories.toFixed(0);
  return Number(caloriesBurnt.textContent);
}

function renderTrainingItems() {
  trainingList.innerHTML = "";

  if (!trainingItems.length) {
    const item = document.createElement("li");
    item.textContent = "No training items added yet.";
    trainingList.appendChild(item);
    return;
  }

  trainingItems.forEach((entry, index) => {
    const li = document.createElement("li");

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.checked = entry.done;
    checkbox.addEventListener("change", () => {
      trainingItems[index].done = checkbox.checked;
    });

    const text = document.createElement("span");
    text.textContent = entry.title;

    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.textContent = "Delete";
    removeBtn.addEventListener("click", () => {
      trainingItems.splice(index, 1);
      renderTrainingItems();
    });

    li.append(checkbox, text, removeBtn);
    trainingList.appendChild(li);
  });
}

function getEntries() {
  return JSON.parse(localStorage.getItem(entriesKey) || "[]");
}

function setEntries(entries) {
  localStorage.setItem(entriesKey, JSON.stringify(entries));
}

function renderSavedEntries() {
  const entries = getEntries();
  savedEntries.innerHTML = "";

  if (!entries.length) {
    savedEntries.innerHTML = "<p>No saved entries yet.</p>";
    return;
  }

  entries
    .slice()
    .sort((a, b) => b.date.localeCompare(a.date))
    .forEach((entry) => {
      const card = document.createElement("article");
      card.className = "entry";
      const trainingHtml = entry.training.length
        ? `<ul>${entry.training
            .map(
              (item) =>
                `<li>${item.done ? "✅" : "⬜"} ${item.title.replace(/</g, "&lt;")}</li>`
            )
            .join("")}</ul>`
        : "<p>No training recorded.</p>";

      card.innerHTML = `
        <p><strong>${entry.date}</strong></p>
        <p>Steps: ${entry.steps} | Water: ${entry.water} L | Sleep: ${entry.sleep} hrs</p>
        <p>Calories Burnt: ${entry.calories} kcal</p>
        <div>${trainingHtml}</div>
      `;

      savedEntries.appendChild(card);
    });
}

function clearInputs() {
  stepsInput.value = "";
  waterInput.value = "";
  sleepInput.value = "";
  trainingInput.value = "";
  trainingItems = [];
  renderTrainingItems();
  estimateCalories();
}

addTrainingBtn.addEventListener("click", () => {
  const title = trainingInput.value.trim();
  if (!title) {
    return;
  }

  trainingItems.push({ title, done: false });
  trainingInput.value = "";
  renderTrainingItems();
});

[stepsInput, heightInput, weightInput].forEach((el) => {
  el.addEventListener("input", estimateCalories);
});

[heightInput, weightInput].forEach((el) => {
  el.addEventListener("change", saveProfile);
});

saveBtn.addEventListener("click", () => {
  const date = dateInput.value || todayISO();

  const entry = {
    date,
    steps: Number(stepsInput.value) || 0,
    water: Number(waterInput.value) || 0,
    sleep: Number(sleepInput.value) || 0,
    calories: estimateCalories(),
    training: trainingItems,
  };

  const entries = getEntries().filter((existing) => existing.date !== date);
  entries.push(entry);
  setEntries(entries);
  renderSavedEntries();
  clearInputs();
});

clearBtn.addEventListener("click", clearInputs);

function init() {
  loadProfile();
  dateInput.value = todayISO();
  renderTrainingItems();
  estimateCalories();
  renderSavedEntries();
}

init();
