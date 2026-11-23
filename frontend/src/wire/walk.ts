// Every value of a record, found through its type in the schema, nested records and lists included.
import { recordNamed, type TypeRef } from './schema';

export function walkRecord(value: Record<string, unknown> | null | undefined, recordName: string | null, visit: (field: unknown, type: TypeRef, name: string) => void, depth = 0) {
  if (!value || depth > 4) {
    return;
  }
  const record = recordNamed(typeof value.kind === 'string' ? value.kind : (recordName ?? ''));
  for (const component of record?.components ?? []) {
    walkValue(value[component.name], component.type, component.name, visit, depth);
  }
}

function walkValue(field: unknown, type: TypeRef, name: string, visit: (field: unknown, type: TypeRef, name: string) => void, depth: number) {
  if (field === null || field === undefined) {
    return;
  }
  if ((type.t === 'list' || type.t === 'set') && Array.isArray(field)) {
    field.forEach((element) => walkValue(element, type.of as TypeRef, name, visit, depth));
  } else if (type.t === 'ref' && typeof field === 'object' && type.name !== 'SkillCard') {
    walkRecord(field as Record<string, unknown>, type.name ?? null, visit, depth + 1);
  } else {
    visit(field, type, name);
  }
}
