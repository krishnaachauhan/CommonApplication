# cat json_to_excel.py 

import json

import pandas as pd
 
# Input/output paths

INPUT_JSON =  "issues.json" #"issues.json"

OUTPUT_XLSX = "issues_report.xlsx"
 
# Load JSON

with open(INPUT_JSON, "r") as f:

    data = json.load(f)
 
issues = data.get("issues", [])
 
# Convert to DataFrame

df = pd.DataFrame([{

    "Key": issue.get("key"),

    "Severity": issue.get("severity"),

    "Type": issue.get("type"),

    "Message": issue.get("message"),

    "File": issue.get("component"),

    "Line": issue.get("line", ""),

    "Author": issue.get("author", ""),

    "Status": issue.get("status")

} for issue in issues])
 
# Save as Excel

df.to_excel(OUTPUT_XLSX, index=False)

print(f" Excel file generated: {OUTPUT_XLSX}")

 
