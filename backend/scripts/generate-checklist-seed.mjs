import fs from "node:fs";

const inputPath = process.argv[2];

if (!inputPath) {
    throw new Error(
        "사용법: node generate-checklist-seed.mjs checklist-dummy-data.json"
    );
}

const source = JSON.parse(
    fs.readFileSync(inputPath, "utf8")
);

const stageMap = {
    prenatal: "PREGNANCY",
    baby: "AGE_0_TO_1",
    toddler: "AGE_2_TO_4",
    preschool: "AGE_5_TO_7",
    childhood: "AGE_8_TO_10",
    earlyTeen: "AGE_11_TO_13",
    teen: "AGE_14_TO_16",
    future: "AGE_17_TO_19"
};

const sqlString = value => {
    if (value === null || value === undefined) {
        return "NULL";
    }

    const escaped = String(value)
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "''");

    return `'${escaped}'`;
};

const stageOrders = new Map();

const templateValues = source.checklistItems.map(item => {
    const stage = stageMap[item.stageId];

    if (!stage) {
        throw new Error(
            `지원하지 않는 stageId: ${item.stageId}`
        );
    }

    const itemOrder =
        (stageOrders.get(stage) ?? 0) + 1;

    stageOrders.set(stage, itemOrder);

    const content =
        item.infoDescription ?? item.description;

    return `(
        ${sqlString(item.id)},
        ${sqlString(stage)},
        ${sqlString(item.category.toUpperCase())},
        ${sqlString(item.title)},
        ${sqlString(item.description)},
        ${sqlString(content)},
        ${sqlString(item.actionType.toUpperCase())},
        ${sqlString(item.route ?? null)},
        ${sqlString(item.infoTitle ?? null)},
        ${sqlString(item.infoNotice ?? null)},
        ${itemOrder},
        1
    )`;
});

const detailStatements = [];

for (const item of source.checklistItems) {
    const infoItems = item.infoItems ?? [];

    infoItems.forEach((detail, index) => {
        detailStatements.push(`
INSERT INTO checklist_item_detail (
    checklist_item_template_id,
    title,
    description,
    action_label,
    external_url,
    detail_content,
    item_order
)
SELECT
    checklist_item_template_id,
    ${sqlString(detail.title)},
    ${sqlString(detail.description)},
    ${sqlString(detail.actionLabel ?? null)},
    ${sqlString(detail.externalUrl ?? null)},
    ${sqlString(detail.detail ?? null)},
    ${index + 1}
FROM checklist_item_template
WHERE template_key = ${sqlString(item.id)}
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    action_label = VALUES(action_label),
    external_url = VALUES(external_url),
    detail_content = VALUES(detail_content),
    updated_at = CURRENT_TIMESTAMP(6);
`);
    });
}

const sql = `
USE azas;

START TRANSACTION;

INSERT INTO checklist_item_template (
    template_key,
    lifecycle_stage,
    category,
    title,
    description,
    detail_content,
    action_type,
    action_url,
    info_title,
    info_notice,
    item_order,
    is_active
)
VALUES
${templateValues.join(",\n")}
ON DUPLICATE KEY UPDATE
    lifecycle_stage = VALUES(lifecycle_stage),
    category = VALUES(category),
    title = VALUES(title),
    description = VALUES(description),
    detail_content = VALUES(detail_content),
    action_type = VALUES(action_type),
    action_url = VALUES(action_url),
    info_title = VALUES(info_title),
    info_notice = VALUES(info_notice),
    item_order = VALUES(item_order),
    is_active = VALUES(is_active),
    updated_at = CURRENT_TIMESTAMP(6);

${detailStatements.join("\n")}

COMMIT;
`;

process.stdout.write(sql.trimStart());