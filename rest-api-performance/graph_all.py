import pandas as pd
import matplotlib.pyplot as plt
import os
import sys

# PART 1 — INTEROPERABILITY GRAPHS

CSV_DIR_INTER = "csv_files/interoperability_csv"
GRAPH_INTER = "graphs/interoperability"
os.makedirs(GRAPH_INTER, exist_ok=True)

def maybe_plot(df, col, label):
    if col in df.columns:
        plt.plot(df["t"], df[col], label=label)
    else:
        print(f"⚠ Missing column: {col}")

print("=== Processing interoperability CSV files ===")

inter_files = [f for f in os.listdir(CSV_DIR_INTER) if f.endswith(".csv")]

if not inter_files:
    print("⚠ No interoperability CSV files found.")
else:
    print("Found:", inter_files)

for file in inter_files:
    csv_path = os.path.join(CSV_DIR_INTER, file)
    base = os.path.splitext(file)[0]
    endpoint = base.replace("_metrics", "")

    OUT = os.path.join(GRAPH_INTER, endpoint)
    os.makedirs(OUT, exist_ok=True)

    print(f"Processing {file} → {OUT}/")

    df = pd.read_csv(csv_path)
    df["t"] = df["timestamp"] - df["timestamp"].min()

    # --- Transaction Time ---
    plt.figure(figsize=(12,6))
    maybe_plot(df, "add_ms", "Add")
    maybe_plot(df, "update_ms", "Update")
    maybe_plot(df, "delete_ms", "Delete")
    plt.title(f"{endpoint} — Transaction Time (ms)")
    plt.xlabel("Time Offset (ms)")
    plt.ylabel("Time (ms)")
    plt.legend(); plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT}/{endpoint}_transaction.png", dpi=300)
    plt.close()

    # --- CPU ---
    plt.figure(figsize=(12,6))
    maybe_plot(df, "add_cpu", "CPU Add")
    maybe_plot(df, "update_cpu", "CPU Update")
    maybe_plot(df, "delete_cpu", "CPU Delete")
    plt.title(f"{endpoint} — CPU Usage (%)")
    plt.xlabel("Time Offset (ms)")
    plt.ylabel("CPU (%)")
    plt.legend(); plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT}/{endpoint}_cpu.png", dpi=300)
    plt.close()

    # --- Memory ---
    plt.figure(figsize=(12,6))
    maybe_plot(df, "add_mem", "Memory Add")
    maybe_plot(df, "update_mem", "Memory Update")
    maybe_plot(df, "delete_mem", "Memory Delete")
    plt.title(f"{endpoint} — Memory (MB)")
    plt.xlabel("Time Offset (ms)")
    plt.ylabel("Memory (MB)")
    plt.legend(); plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT}/{endpoint}_memory.png", dpi=300)
    plt.close()

    print(f"✔ Saved graphs for {endpoint}")

# PART 2 — BASIC CATEGORY / PROJECT / TODO GRAPHS

CSV_DIR_BASIC = "csv_files"
GRAPH_BASIC = "graphs/basic"
os.makedirs(GRAPH_BASIC, exist_ok=True)

BASIC_ENDPOINTS = {
    "category": "CategoryTests/category_metrics.csv",
    "project":  "ProjectTests/project_metrics.csv",
    "todo":     "TodoTests/todo_metrics.csv"
}

print("\n=== Processing basic endpoint CSV files ===")

for endpoint, rel_path in BASIC_ENDPOINTS.items():

    csv_path = os.path.join(CSV_DIR_BASIC, rel_path)

    if not os.path.exists(csv_path):
        print(f"⚠ Missing CSV: {csv_path}")
        continue

    print(f"Processing {csv_path}")

    df = pd.read_csv(csv_path)
    df["t"] = df["timestamp"] - df["timestamp"].min()

    OUT = os.path.join(GRAPH_BASIC, endpoint)
    os.makedirs(OUT, exist_ok=True)

    # --- Transaction ---
    plt.figure(figsize=(12,6))
    maybe_plot(df, "add_ms", "Add")
    maybe_plot(df, "update_ms", "Update")
    maybe_plot(df, "delete_ms", "Delete")
    plt.title(f"{endpoint.capitalize()} — Transaction Time")
    plt.xlabel("Time Offset (ms)"); plt.ylabel("Time (ms)")
    plt.legend(); plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT}/{endpoint}_transaction.png", dpi=300)
    plt.close()

    # --- CPU ---
    plt.figure(figsize=(12,6))
    maybe_plot(df, "add_cpu", "CPU Add")
    maybe_plot(df, "update_cpu", "CPU Update")
    maybe_plot(df, "delete_cpu", "CPU Delete")
    plt.title(f"{endpoint.capitalize()} — CPU Usage")
    plt.xlabel("Time Offset (ms)"); plt.ylabel("CPU (%)")
    plt.legend(); plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT}/{endpoint}_cpu.png", dpi=300)
    plt.close()

    # --- Memory ---
    plt.figure(figsize=(12,6))
    maybe_plot(df, "add_mem", "Memory Add")
    maybe_plot(df, "update_mem", "Memory Update")
    maybe_plot(df, "delete_mem", "Memory Delete")
    plt.title(f"{endpoint.capitalize()} — Memory")
    plt.xlabel("Time Offset (ms)"); plt.ylabel("Memory (MB)")
    plt.legend(); plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{OUT}/{endpoint}_memory.png", dpi=300)
    plt.close()

    print(f"✔ Saved basic graphs for {endpoint}")

print("Interoperability graphs → graphs/interoperability/")
print("Basic endpoint graphs → graphs/basic/")
