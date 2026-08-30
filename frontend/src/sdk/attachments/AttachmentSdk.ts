import { BaseSdk } from '../common/BaseSdk';

export interface AttachmentItem {
  id: string;
  ownerType: 'PLANNER_TASK' | 'NOTICE';
  ownerId: string;
  uploaderId: string;
  originalFileName: string;
  contentType: string;
  fileSize: number;
  downloadUrl: string;
  createdAt?: string;
}

export class AttachmentSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/attachments';

  public async uploadAttachment(
    file: File,
    ownerType: 'PLANNER_TASK' | 'NOTICE',
    ownerId: string
  ): Promise<AttachmentItem> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('ownerType', ownerType);
    formData.append('ownerId', ownerId);

    return this.post<AttachmentItem>(`${this.baseUrl}/upload`, formData);
  }

  public async getAttachmentsForOwner(
    ownerType: 'PLANNER_TASK' | 'NOTICE',
    ownerId: string
  ): Promise<AttachmentItem[]> {
    return this.get<AttachmentItem[]>(this.baseUrl, { ownerType, ownerId });
  }

  public async getAttachmentById(id: string): Promise<AttachmentItem> {
    return this.get<AttachmentItem>(`${this.baseUrl}/${id}`);
  }

  public async deleteAttachment(id: string): Promise<void> {
    return this.delete<void>(`${this.baseUrl}/${id}`);
  }

  public getDownloadUrl(id: string): string {
    return `${this.baseUrl}/${id}/download`;
  }

  public getViewUrl(id: string): string {
    return `${this.baseUrl}/${id}/view`;
  }
}

export const attachmentSdk = new AttachmentSdk();
